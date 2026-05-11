package com.alexey.tabgenerator.controller;

import com.alexey.tabgenerator.dto.request.LoginRequest;
import com.alexey.tabgenerator.dto.request.NewPasswordRequest;
import com.alexey.tabgenerator.dto.request.RecoverPasswordRequest;
import com.alexey.tabgenerator.dto.request.RegisterRequest;
import com.alexey.tabgenerator.dto.response.AuthResponse;
import com.alexey.tabgenerator.dto.response.CheckRecoverPasswordTokenResponse;
import com.alexey.tabgenerator.dto.response.RecoverPasswordResponse;
import com.alexey.tabgenerator.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
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
 */
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
        @Valid @RequestBody RegisterRequest request,
        HttpServletRequest httpRequest
    ) {

        log.debug("[{}] Запрос на регистрацию: username={}, email={}",
            httpRequest.getRemoteAddr(), request.getUsername(), request.getEmail());

        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Аутентификация пользователя (вход в систему).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest
    ) {

        log.debug("[{}] Запрос на вход: username/email={}",
            httpRequest.getRemoteAddr(), request.getUsernameOrEmail());

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Отправка сообщения с ссылкой на восстановление пароля на почту
     */
    @PostMapping("/recover-password")
    public ResponseEntity<RecoverPasswordResponse> sendRecoverPasswordMail(
        @Valid @RequestBody RecoverPasswordRequest request,
        HttpServletRequest httpRequest
    ) {

        log.debug("[{}] Запрос на восстановление пароля: email={}",
            httpRequest.getRemoteAddr(), request.getEmail());

        RecoverPasswordResponse response = authService.sendRecoverPasswordMail(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Проверка действительности токена восстановления пароля
     */
    @GetMapping("/recover-password/{token}")
    public ResponseEntity<CheckRecoverPasswordTokenResponse> checkRecoverPasswordToken(
        @PathVariable String token,
        HttpServletRequest httpRequest
    ) {

        log.debug("[{}] Запрос на проверку валидности токена восстановления пароля",
            httpRequest.getRemoteAddr());

        CheckRecoverPasswordTokenResponse response =
            authService.checkRecoverPasswordToken(token);

        return ResponseEntity.ok(response);
    }

    /**
     * Восстановление пароля пользователя через email.
     */
    @PostMapping("/recover-password/{token}")
    public ResponseEntity<Void> recoverPassword(
        @Valid @RequestBody NewPasswordRequest request,
        @PathVariable String token,
        HttpServletRequest httpRequest
    ) {

        log.debug("[{}] Запрос на смену пароля по ссылке",
            httpRequest.getRemoteAddr());

        authService.recoverPassword(request, token);

        return ResponseEntity.noContent().build();
    }
}