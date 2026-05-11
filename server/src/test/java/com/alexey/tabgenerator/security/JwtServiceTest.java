package com.alexey.tabgenerator.security;

import com.alexey.tabgenerator.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для {@link JwtService}.
 *
 * Проверяют:
 * - генерацию JWT
 * - извлечение username из токена
 * - валидацию токена
 * - построение объекта Authentication
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testUser1")
            .email("testuser1@mail.ru")
            .passwordHash("$2a$10$OomqQ6A0HEpAcBkvzD2ebeCLF4L/qXGUWwumAFov.Mzha77srmsQy")
            .build();
    }

    @Test
    @DisplayName("Генерация JWT токена: успех")
    void generateToken_success() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token, "JWT токен не должен быть null");
    }

    @Test
    @DisplayName("Извлечение username из JWT токена: успех")
    void extractUsername_success() {
        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);

        assertEquals(testUser.getUsername(), username,
            "Username из токена должен совпадать с пользователем"
        );
    }

    @Test
    @DisplayName("Проверка валидности JWT токена: успех")
    void isTokenValid_success() {
        String token = jwtService.generateToken(testUser);
        UserDetails userDetails = userDetailsService.
            loadUserByUsername(testUser.getUsername());

        assertTrue(jwtService.isTokenValid(token, userDetails),
            "Токен должен быть валиден для правильного пользователя"
        );
    }

    @Test
    @DisplayName("Проверка валидности JWT токена: токен невалиден для другого пользователя")
    void isTokenValid_fail_tokenInvalidForAnotherUser() {
        String token = jwtService.generateToken(testUser);
        UserDetails userDetails = userDetailsService.
            loadUserByUsername("alexeyTest");

        assertFalse(jwtService.isTokenValid(token, userDetails),
            "Токен не должен быть валиден для другого пользователя"
        );
    }

    @Test
    @DisplayName("Создание Authentication: успех")
    void buildAuthentication_success() {
        UserDetails userDetails = userDetailsService.
            loadUserByUsername(testUser.getUsername());
        UsernamePasswordAuthenticationToken auth = jwtService.buildAuthentication(userDetails);

        assertEquals(userDetails, auth.getPrincipal(),
            "Principal должен совпадать с UserDetails"
        );
        assertIterableEquals(userDetails.getAuthorities(),
            auth.getAuthorities(),
            "Authorities должны совпадать по содержимому"
        );
        assertNull(auth.getCredentials(), "Credentials должны быть null");
    }
}