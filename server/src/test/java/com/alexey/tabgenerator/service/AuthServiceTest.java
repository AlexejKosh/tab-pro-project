package com.alexey.tabgenerator.service;

import com.alexey.tabgenerator.dto.request.LoginRequest;
import com.alexey.tabgenerator.dto.request.RecoverPasswordRequest;
import com.alexey.tabgenerator.dto.request.RegisterRequest;
import com.alexey.tabgenerator.dto.response.AuthResponse;
import com.alexey.tabgenerator.dto.response.RecoverPasswordResponse;
import com.alexey.tabgenerator.entity.User;
import com.alexey.tabgenerator.exception.DuplicateEntityException;
import com.alexey.tabgenerator.exception.UnauthorizedException;
import com.alexey.tabgenerator.repository.UserRepository;
import com.alexey.tabgenerator.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для {@link AuthService}.
 *
 * Проверяют:
 * - регистрацию пользователя
 * - авторизацию пользователя
 * - восстановление пароля по email
 *
 * Используют моки: {@link UserRepository},
 * {@link PasswordEncoder}, {@link JwtService},
 * {@link EmailService}.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // Мок репозитория пользователей для
    // имитации работы с базой данных
    @Mock
    private UserRepository userRepository;

    // Мок кодировщика паролей, чтобы
    // не выполнять реальное хеширование
    @Mock
    private PasswordEncoder passwordEncoder;

    // Мок сервиса JWT для имитации
    // генерации токенов при авторизации
    @Mock
    private JwtService jwtService;

    // Мок сервиса отправки email,
    // чтобы не отправлять реальные письма
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("encodedPassword")
            .build();
    }

    @Test
    @DisplayName("Регистрация: успех")
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertEquals("jwt-token", response.getToken());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Регистрация: username уже существует")
    void register_fail_duplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(DuplicateEntityException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Регистрация: email уже существует")
    void register_fail_duplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateEntityException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Логин: успех")
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("password123");

        when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
            .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword"))
            .thenReturn(true);
        when(jwtService.generateToken(mockUser)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        verify(jwtService).generateToken(mockUser);
    }

    @Test
    @DisplayName("Логин: неверные учетные данные")
    void login_fail_userNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("nonexistent");
        request.setPassword("password");

        when(userRepository.findByUsernameOrEmail("nonexistent", "nonexistent"))
            .thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Восстановление пароля: успех")
    void recoverPassword_success() {
        RecoverPasswordRequest request = new RecoverPasswordRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        RecoverPasswordResponse response = authService.recoverPassword(request);

        assertEquals("Новый пароль для входа отправлен на почту.", response.getMessage());
        verify(emailService).sendPasswordRecovery(eq("test@example.com"), anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Восстановление пароля: пользователь не найден")
    void recoverPassword_fail_userNotFound() {
        RecoverPasswordRequest request = new RecoverPasswordRequest();
        request.setEmail("notfound@example.com");

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        RecoverPasswordResponse response = authService.recoverPassword(request);

        assertEquals("Пользователь с данным email не найден.", response.getMessage());
        verify(emailService, never()).sendPasswordRecovery(anyString(), anyString());
    }
}