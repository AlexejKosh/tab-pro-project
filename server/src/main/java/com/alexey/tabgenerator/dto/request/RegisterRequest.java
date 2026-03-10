package com.alexey.tabgenerator.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для запроса регистрации нового пользователя.
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 25, message = "Имя пользователя должно содержать от 3 до 25 символов")
    private String username;

    @NotBlank(message = "Email обязательно")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, max = 100, message = "Пароль должен содержать минимум 6 символов")
    private String password;
}