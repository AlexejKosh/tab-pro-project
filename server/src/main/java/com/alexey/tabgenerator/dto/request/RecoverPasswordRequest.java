package com.alexey.tabgenerator.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для запроса восстановления пароля пользователя через email.
 */
@Getter
@Setter
public class RecoverPasswordRequest {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;
}