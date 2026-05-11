package com.alexey.tabgenerator.security;

import com.alexey.tabgenerator.entity.User;
import com.alexey.tabgenerator.exception.UnauthorizedException;
import com.alexey.tabgenerator.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для {@link SecurityUtils}.
 *
 * Проверяют:
 * - получение текущего пользователя из SecurityContext
 * - обработку отсутствия аутентификации
 * - обработку случая, когда пользователь отсутствует в БД
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SecurityUtilsTest {

    @Autowired
    private UserRepository userRepository;

    private SecurityUtils securityUtils;

    @BeforeEach
    void setUp() {
        securityUtils = new SecurityUtils(userRepository);
    }

    @Test
    @DisplayName("Получение текущего пользователя: успех")
    void getCurrentUser_success() {
        String username = "testUser1";
        var authToken = new UsernamePasswordAuthenticationToken(
            username,
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(authToken);

        User user = securityUtils.getCurrentUser();

        assertNotNull(user, "Пользователь не должен быть null");
        assertEquals(username, user.getUsername(), "Username должен совпадать");
    }

    @Test
    @DisplayName("Получение текущего пользователя: отстутствует аутентификация")
    void getCurrentUser_fail_notAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(null);

        assertThrows(UnauthorizedException.class, () -> securityUtils.getCurrentUser(),
            "Должен выброситься UnauthorizedException при отсутствии аутентификации");
    }

    @Test
    @DisplayName("Получение текущего пользователя: пользователь не найден в БД")
    void getCurrentUser_fail_userNotFound() {
        String fakeUsername = "nonexistentUser";
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(fakeUsername, null)
        );

        assertThrows(UnauthorizedException.class, () -> securityUtils.getCurrentUser(),
            "Должен выброситься UnauthorizedException, если пользователь не найден в БД");
    }
}