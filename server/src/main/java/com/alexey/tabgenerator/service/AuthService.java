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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * Сервис для работы с аутентификацией и восстановлением пароля пользователей.
 * Обрабатывает регистрацию, авторизацию и отправку новых паролей по email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Регистрация нового пользователя.
     * Проверяет уникальность username и email, сохраняет пользователя и возвращает JWT токен.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        log.debug("Регистрация пользователя: username={}", request.getUsername());

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

        log.info("Пользователь успешно зарегистрирован: username={}", user.getUsername());

        return new AuthResponse(token);
    }

    /**
     * Авторизация пользователя.
     * Проверяет корректность логина/пароля и возвращает JWT токен.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        log.debug("Авторизация для  пользователя: username/email={}", request.getUsernameOrEmail());

        String usernameOrEmail = request.getUsernameOrEmail();

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    return new UnauthorizedException("Неверный логин или пароль");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Неверный логин или пароль");
        }

        String token = jwtService.generateToken(user);

        log.info("Пользователь успешно авторизован: username={}", user.getUsername());

        return new AuthResponse(token);
    }

    /**
     * Восстановление пароля пользователя.
     * Если email найден, генерирует новый пароль, сохраняет его и отправляет на почту.
     */
    @Transactional
    public RecoverPasswordResponse recoverPassword(RecoverPasswordRequest request) {

        log.debug("Восстановление пароля: email={}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        String message;

        if (user != null) {
            String newPassword = generateRandomPassword();
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            log.info("Новый пароль сгенерирован: username={}", user.getUsername());

            emailService.sendPasswordRecovery(user.getEmail(), newPassword);
            message = "Новый пароль для входа отправлен на почту.";

            log.info("Восстановление пароля инициировано: email={}", request.getEmail());
        } else {
            message = "Пользователь с данным email не найден.";

            log.warn("Попытка восстановления пароля для несуществующего email: email={}", request.getEmail());
        }

        return new RecoverPasswordResponse(message);
    }

    /**
     * Генерация случайного пароля заданной длины
     */
    private String generateRandomPassword() {

        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        return sb.toString();
    }
}