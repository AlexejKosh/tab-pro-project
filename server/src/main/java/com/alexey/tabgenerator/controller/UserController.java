package com.alexey.tabgenerator.controller;

import com.alexey.tabgenerator.dto.request.ChangePasswordRequest;
import com.alexey.tabgenerator.dto.response.UserResponse;
import com.alexey.tabgenerator.service.UserService;

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
    public ResponseEntity<UserResponse> getCurrentUser() {

        log.debug("Запрос информации для текущего пользователя");

        UserResponse response = userService.getCurrentUser();

        log.info("Запрос информации для текущего пользователя успешно обработан: username={}",
            response.getUsername());

        return ResponseEntity.ok(response);
    }

    /**
     * Изменение пароля текущего пользователя.
     */
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
        @Valid @RequestBody ChangePasswordRequest request
    ) {

        log.debug("Запрос на смену пароля");

        userService.changePassword(request);

        log.info("Запрос на смену пароля успешно обработан");

        return ResponseEntity.ok().build();
    }

    /**
     * Удаление аккаунта текущего пользователя.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser() {

        log.debug("Запрос на удаление текущего пользователя");

        userService.deleteCurrentUser();

        log.info("Запрос на удаление текущего пользователя успешно обработан");

        return ResponseEntity.noContent().build();
    }
}