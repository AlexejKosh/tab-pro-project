package com.alexey.tabgenerator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для запроса смены пароля пользователя.
 */
@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "Старый пароль обязателен")
    private String oldPassword;

    @NotBlank(message = "Новый пароль обязателен")
    @Size(min = 6, max = 100, message = "Новый пароль должен содержать минимум 6 символов")
    private String newPassword;
}