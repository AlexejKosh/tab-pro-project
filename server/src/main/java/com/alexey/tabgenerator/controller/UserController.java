package com.alexey.tabgenerator.controller;

import com.alexey.tabgenerator.dto.request.ChangePasswordRequest;
import com.alexey.tabgenerator.dto.response.UserResponse;
import com.alexey.tabgenerator.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для работы с текущим пользователем.
 * Обрабатывает запросы получения информации о пользователе,
 * изменения пароля и удаления аккаунта.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Получение информации о текущем авторизованном пользователе.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
        HttpServletRequest httpRequest
    ) {

        log.debug("[{}] Запрос информации для текущего пользователя",
            httpRequest.getRemoteAddr()
        );

        UserResponse response = userService.getCurrentUser();

        return ResponseEntity.ok(response);
    }

    /**
     * Изменение пароля текущего пользователя.
     */
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        HttpServletRequest httpRequest
    ) {

        log.debug("[{}] Запрос на смену пароля",
            httpRequest.getRemoteAddr()
        );

        userService.changePassword(request);

        return ResponseEntity.ok().build();
    }

    /**
     * Удаление аккаунта текущего пользователя.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(
        HttpServletRequest httpRequest
    ) {

        log.debug("[{}] Запрос на удаление текущего пользователя",
            httpRequest.getRemoteAddr()
        );

        userService.deleteCurrentUser();

        return ResponseEntity.noContent().build();
    }
}