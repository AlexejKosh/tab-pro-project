package com.alexey.tabgenerator.service;

import com.alexey.tabgenerator.dto.request.LoginRequest;
import com.alexey.tabgenerator.dto.request.NewPasswordRequest;
import com.alexey.tabgenerator.dto.request.RecoverPasswordRequest;
import com.alexey.tabgenerator.dto.request.RegisterRequest;
import com.alexey.tabgenerator.dto.response.AuthResponse;
import com.alexey.tabgenerator.dto.response.CheckRecoverPasswordTokenResponse;
import com.alexey.tabgenerator.dto.response.RecoverPasswordResponse;
import com.alexey.tabgenerator.entity.PasswordResetToken;
import com.alexey.tabgenerator.entity.User;
import com.alexey.tabgenerator.exception.*;
import com.alexey.tabgenerator.repository.PasswordResetTokenRepository;
import com.alexey.tabgenerator.repository.UserRepository;
import com.alexey.tabgenerator.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Сервис для работы с аутентификацией и восстановлением пароля пользователей.
 * Обрабатывает регистрацию, авторизацию и восстановление паролей по email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Регистрация нового пользователя.
     * Проверяет уникальность username и email, сохраняет пользователя и возвращает JWT токен.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        log.debug("Регистрация пользователя: username={}, email={}",
            request.getUsername(), request.getEmail());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateEntityException("Username уже занят");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEntityException("Email уже зарегистрирован");
        }

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        log.info("Пользователь успешно зарегистрирован: id={}, username={}",
            user.getId(), user.getUsername());

        return new AuthResponse(token);
    }

    /**
     * Авторизация пользователя.
     * Проверяет корректность логина/пароля и возвращает JWT токен.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        log.debug("Попытка авторизации для  пользователя: username/email={}", request.getUsernameOrEmail());

        String usernameOrEmail = request.getUsernameOrEmail();

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    return new UnauthorizedException("Неверный логин или пароль");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Неверный логин или пароль");
        }

        String token = jwtService.generateToken(user);

        log.info("Пользователь успешно авторизован: id={}, username={}",
            user.getId(), user.getUsername());

        return new AuthResponse(token);
    }

    /**
     * Инициализация восстановления пароля пользователя.
     * Если email найден в системе, то отправляет на почту ссылку для восстановления.
     */
    @Transactional
    public RecoverPasswordResponse sendRecoverPasswordMail(RecoverPasswordRequest request) {

        log.debug(
            "Инициация восстановления пароля: email={}",
            request.getEmail()
        );

        passwordResetTokenRepository.deleteByExpiresAtBefore(OffsetDateTime.now());

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        String message;

        if (user != null) {
            passwordResetTokenRepository.deleteByUser(user);
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(DigestUtils.sha256Hex(token))
                .expiresAt(OffsetDateTime.now().plusMinutes(30))
                .build();

            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordRecovery(user.getEmail(), token);

            log.info(
                "Токен восстановления создан и отправлен: userId={}",
                user.getId()
            );
            log.info(
                "{}", token
            );

            message = "Ссылка для восставноления пароля отправлена на почту.";
        } else {
            log.warn(
                "Попытка восстановления пароля для несуществующего email={}",
                request.getEmail()
            );

            message = "Пользователь с данным email не найден.";
        }

        return new RecoverPasswordResponse(message);
    }

    /**
     * Проверка валидности токена восстановления пароля
     */
    @Transactional
    public CheckRecoverPasswordTokenResponse checkRecoverPasswordToken(
        String token
    ) {

        log.debug("Проверка валидности токена сброса пароля: tokenHash={}",
            DigestUtils.sha256Hex(token));

        passwordResetTokenRepository.deleteByExpiresAtBefore(OffsetDateTime.now());

        PasswordResetToken resetToken =
            passwordResetTokenRepository.findByToken(DigestUtils.sha256Hex(token)).orElse(null);

        return new CheckRecoverPasswordTokenResponse(resetToken != null);
    }

    /**
     * Завершение процесса восстановления пароля пользователя.
     * При успешной проверке обновляет пароль пользователя и удаляет токен.
     */
    @Transactional
    public void recoverPassword(NewPasswordRequest request, String token) {

        log.debug("Попытка сброса пароля по токену");

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(DigestUtils.sha256Hex(token))
            .orElseThrow(() -> new NotFoundException("Токен не найден"));

        if (resetToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new TokenExpiredException("Срок действия токена истёк");
        }

        if (!Objects.equals(request.getPassword1(), request.getPassword2())) {
            throw new PasswordMismatchException("Пароли не совпадают");
        }

        User user = resetToken.getUser();

        user.setPasswordHash(passwordEncoder.encode(request.getPassword1()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        log.info(
            "Пароль успешно сброшен: userId={}",
            user.getId()
        );
    }
}
