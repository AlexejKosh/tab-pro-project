package com.alexey.tabgenerator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO для ответа при аутентификации пользователя.
 * Содержит JWT токен, который возвращается после успешного логина или регистрации.
 */
@Getter
@AllArgsConstructor
public class AuthResponse {

    private final String token;
}
