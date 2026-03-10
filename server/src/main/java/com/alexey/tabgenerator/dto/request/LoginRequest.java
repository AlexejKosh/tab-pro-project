package com.alexey.tabgenerator.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для запроса аутентификации пользователя (логин).
 */
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Поле username/email обязательно")
    private String usernameOrEmail;

    @NotBlank(message = "Пароль обязателен")
    private String password;
}