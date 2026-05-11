package com.alexey.tabgenerator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewPasswordRequest {

    @NotBlank(message = "Пароль 1 обязателен")
    @Size(min = 6, max = 100, message = "Пароль должен содержать минимум 6 символов")
    private String password1;

    @NotBlank(message = "Пароль 2 обязателен")
    @Size(min = 6, max = 100, message = "Пароль должен содержать минимум 6 символов")
    private String password2;
}
