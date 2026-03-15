package com.alexey.tabgenerator.service;

import com.alexey.tabgenerator.dto.request.ChangePasswordRequest;
import com.alexey.tabgenerator.dto.response.UserResponse;
import com.alexey.tabgenerator.entity.User;
import com.alexey.tabgenerator.exception.UnauthorizedException;
import com.alexey.tabgenerator.repository.UserRepository;
import com.alexey.tabgenerator.security.SecurityUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Интеграционные тесты для {@link UserService}
 *
 * Проверяют:
 * - получение текущего пользователя
 * - смену пароля для текущего пользователя
 * - удаление текущего пользователя
 *
 * Используют моки: {@link UserRepository},
 * {@link PasswordEncoder}, {@link SecurityUtils}.
 */
class UserServiceTest {

    // Мок репозитория для сущности User, чтобы изолировать
    // взаимодействие с БД
    @Mock
    private UserRepository userRepository;

    // Мок кодировщика паролей, чтобы
    // не выполнять реальное хеширование
    @Mock
    private PasswordEncoder passwordEncoder;

    // Мок компонентов безопасности
    // для иммитации авторизованного пользователя
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("encodedPassword")
            .createdAt(OffsetDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Получение текущего пользователя: успех")
    void getCurrentUser_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);

        UserResponse response = userService.getCurrentUser();

        assertEquals(mockUser.getId(), response.getId());
        assertEquals(mockUser.getUsername(), response.getUsername());
        assertEquals(mockUser.getEmail(), response.getEmail());
    }

    @Test
    @DisplayName("Получение текущего пользователя: пользователь не авторизован")
    void getCurrentUser_fail_unauthorized() {
        SecurityContext context = mock(SecurityContext.class);

        when(context.getAuthentication()).thenReturn(null);

        SecurityContextHolder.setContext(context);

        when(securityUtils.getCurrentUser()).thenCallRealMethod();

        UserService us = new UserService(
            userRepository,
            passwordEncoder,
            new SecurityUtils(userRepository)
        );
        UnauthorizedException ex = assertThrows(UnauthorizedException.class, us::getCurrentUser);

        assertEquals("Пользователь не авторизован", ex.getMessage());
    }

    @Test
    @DisplayName("Смена пароля: успех")
    void changePassword_success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass123");

        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("newEncodedPass");

        userService.changePassword(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertEquals("newEncodedPass", savedUser.getPasswordHash());
    }

    @Test
    @DisplayName("Смена пароля: старый пароль неверный")
    void changePassword_fail_wrongOldPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongOld");
        request.setNewPassword("newPass123");

        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("wrongOld", "encodedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.changePassword(request));
    }

    @Test
    @DisplayName("Удаление текущего пользователя: успех")
    void deleteCurrentUser_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);

        userService.deleteCurrentUser();

        verify(userRepository).delete(mockUser);
    }

    @Test
    @DisplayName("Удаление текущего пользователя: пользователь не авторизован")
    void deleteCurrentUser_fail_unauthorized() {
        SecurityContext context = mock(SecurityContext.class);

        when(context.getAuthentication()).thenReturn(null);

        SecurityContextHolder.setContext(context);

        when(securityUtils.getCurrentUser()).thenCallRealMethod();

        UserService us = new UserService(
            userRepository,
            passwordEncoder,
            new SecurityUtils(userRepository)
        );
        UnauthorizedException ex = assertThrows(UnauthorizedException.class, us::deleteCurrentUser);

        assertEquals("Пользователь не авторизован", ex.getMessage());
    }
}