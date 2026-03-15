package com.alexey.tabgenerator.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для {@link UserDetailsServiceImpl}.
 *
 * Проверяют:
 * - загрузку пользователя по username
 * - обработку случая, когда пользователь не найден
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserDetailsServiceImplTest {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private String existingUsername;
    private String nonExistingUsername;

    @BeforeEach
    void setUp() {
        existingUsername = "alexeyKo";
        nonExistingUsername = "nonexistentUser";
    }

    @Test
    @DisplayName("Загрузка пользователя по username: успех")
    void loadUserByUsername_success() {
        UserDetails userDetails = userDetailsService.loadUserByUsername(existingUsername);

        assertNotNull(userDetails, "UserDetails не должен быть null");
        assertEquals(existingUsername, userDetails.getUsername(), "Username должен совпадать");
        assertNotNull(userDetails.getPassword(), "Password не должен быть null");
        assertTrue(userDetails.getAuthorities().isEmpty(), "Authorities должны быть пустыми");
    }

    @Test
    @DisplayName("Загрузка пользователя по username: пользователь не найден")
    void loadUserByUsername_shouldThrowWhenUserNotFound() {

        assertThrows(UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(nonExistingUsername),
            "Должен выброситься UsernameNotFoundException для несуществующего пользователя");
    }
}