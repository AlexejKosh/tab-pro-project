package com.alexey.tabgenerator.controller;

import com.alexey.tabgenerator.dto.request.LoginRequest;
import com.alexey.tabgenerator.dto.request.RecoverPasswordRequest;
import com.alexey.tabgenerator.dto.request.RegisterRequest;
import com.alexey.tabgenerator.dto.response.AuthResponse;
import com.alexey.tabgenerator.dto.response.RecoverPasswordResponse;
import com.alexey.tabgenerator.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер аутентификации пользователей.
 * Обрабатывает запросы регистрации, входа в систему
 * и восстановления пароля.
 * */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Регистрация нового пользователя.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
        @Valid @RequestBody RegisterRequest request
    ) {

        log.debug("Запрос на регистрацию: username={}, email={}",
            request.getUsername(), request.getEmail());

        AuthResponse response = authService.register(request);

        log.info("Запрос на регистрацию успешно обработан: username={}",
            request.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Аутентификация пользователя (вход в систему).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {

        log.debug("Запрос на вход: username/email={}", request.getUsernameOrEmail());

        AuthResponse response = authService.login(request);

        log.info("Запрос на вход успешно обработан: username/email={}",
            request.getUsernameOrEmail());

        return ResponseEntity.ok(response);
    }

    /**
     * Восстановление пароля пользователя через email.
     */
    @PostMapping("/recover-password")
    public ResponseEntity<RecoverPasswordResponse> recoverPassword(
        @Valid @RequestBody RecoverPasswordRequest request
    ) {

        log.debug("Запрос на восстановление пароля: email={}",
            request.getEmail());

        RecoverPasswordResponse response = authService.recoverPassword(request);

        log.info("Запрос на восстановление пароля успешно обработан: email={}",
            request.getEmail());

        return ResponseEntity.ok(response);
    }
}