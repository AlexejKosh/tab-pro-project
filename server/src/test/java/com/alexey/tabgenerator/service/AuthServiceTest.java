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
import com.alexey.tabgenerator.repository.PasswordResetTokenRepository;
import com.alexey.tabgenerator.entity.PasswordResetToken;
import com.alexey.tabgenerator.dto.request.NewPasswordRequest;
import com.alexey.tabgenerator.exception.NotFoundException;
import com.alexey.tabgenerator.exception.TokenExpiredException;
import com.alexey.tabgenerator.exception.PasswordMismatchException;
import org.mockito.ArgumentCaptor;
import com.alexey.tabgenerator.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
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
 * {@link EmailService} {@link PasswordResetTokenRepository}.
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

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

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
    @DisplayName("Отправка письма восстановления: найден пользователь")
    void sendRecoverPasswordMail_success() {
        RecoverPasswordRequest request = new RecoverPasswordRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        doNothing().when(passwordResetTokenRepository).deleteByExpiresAtBefore(any());
        doNothing().when(passwordResetTokenRepository).deleteByUser(any());
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
            .thenAnswer(i -> i.getArgument(0));
        doNothing().when(emailService).sendPasswordRecovery(anyString(), anyString());

        RecoverPasswordResponse response = authService.sendRecoverPasswordMail(request);

        assertEquals("Ссылка для восставноления пароля отправлена на почту.", response.getMessage());

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(captor.capture());
        PasswordResetToken saved = captor.getValue();

        verify(emailService).sendPasswordRecovery(eq(mockUser.getEmail()), eq(saved.getToken()));
    }

    @Test
    @DisplayName("Отправка письма восстановления: пользователь не найден")
    void sendRecoverPasswordMail_userNotFound() {
        RecoverPasswordRequest request = new RecoverPasswordRequest();
        request.setEmail("noone@example.com");

        when(userRepository.findByEmail("noone@example.com")).thenReturn(Optional.empty());

        RecoverPasswordResponse response = authService.sendRecoverPasswordMail(request);

        assertEquals("Пользователь с данным email не найден.", response.getMessage());
        verify(emailService, never()).sendPasswordRecovery(anyString(), anyString());
    }

    @Test
    @DisplayName("Проверка токена восстановления: валидный/невалидный")
    void checkRecoverPasswordToken() {
        doNothing().when(passwordResetTokenRepository).deleteByExpiresAtBefore(any());

        PasswordResetToken tokenEntity = PasswordResetToken.builder()
            .token("tok-1")
            .expiresAt(OffsetDateTime.now().plusMinutes(10))
            .user(mockUser)
            .build();

        when(passwordResetTokenRepository.findByToken("tok-1")).thenReturn(Optional.of(tokenEntity));

        var resp = authService.checkRecoverPasswordToken("tok-1");
        assertEquals(Boolean.TRUE, resp.getValid());

        when(passwordResetTokenRepository.findByToken("tok-2")).thenReturn(Optional.empty());
        var resp2 = authService.checkRecoverPasswordToken("tok-2");
        assertEquals(Boolean.FALSE, resp2.getValid());
    }

    @Test
    @DisplayName("Восстановление пароля: успех")
    void recoverPassword_success() {
        NewPasswordRequest newPass = new NewPasswordRequest();
        newPass.setPassword1("newpass");
        newPass.setPassword2("newpass");

        PasswordResetToken tokenEntity = PasswordResetToken.builder()
            .token("good-token")
            .expiresAt(OffsetDateTime.now().plusMinutes(10))
            .user(mockUser)
            .build();

        when(passwordResetTokenRepository.findByToken("good-token")).thenReturn(Optional.of(tokenEntity));
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNew");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        authService.recoverPassword(newPass, "good-token");

        verify(passwordEncoder).encode("newpass");
        verify(userRepository).save(mockUser);
        verify(passwordResetTokenRepository).delete(tokenEntity);
    }

    @Test
    @DisplayName("Восстановление пароля: токен не найден")
    void recoverPassword_tokenNotFound() {
        NewPasswordRequest newPass = new NewPasswordRequest();
        newPass.setPassword1("a");
        newPass.setPassword2("a");

        when(passwordResetTokenRepository.findByToken("no-token")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authService.recoverPassword(newPass, "no-token"));
    }

    @Test
    @DisplayName("Восстановление пароля: токен просрочен")
    void recoverPassword_tokenExpired() {
        NewPasswordRequest newPass = new NewPasswordRequest();
        newPass.setPassword1("a");
        newPass.setPassword2("a");

        PasswordResetToken tokenEntity = PasswordResetToken.builder()
            .token("expired")
            .expiresAt(OffsetDateTime.now().minusMinutes(1))
            .user(mockUser)
            .build();

        when(passwordResetTokenRepository.findByToken("expired")).thenReturn(Optional.of(tokenEntity));

        assertThrows(TokenExpiredException.class, () -> authService.recoverPassword(newPass, "expired"));
    }

    @Test
    @DisplayName("Восстановление пароля: пароли не совпадают")
    void recoverPassword_passwordMismatch() {
        NewPasswordRequest newPass = new NewPasswordRequest();
        newPass.setPassword1("one");
        newPass.setPassword2("two");

        PasswordResetToken tokenEntity = PasswordResetToken.builder()
            .token("good")
            .expiresAt(OffsetDateTime.now().plusMinutes(10))
            .user(mockUser)
            .build();

        when(passwordResetTokenRepository.findByToken("good")).thenReturn(Optional.of(tokenEntity));

        assertThrows(PasswordMismatchException.class, () -> authService.recoverPassword(newPass, "good"));
    }
}