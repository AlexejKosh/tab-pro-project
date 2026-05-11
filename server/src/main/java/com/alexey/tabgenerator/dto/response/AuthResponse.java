package com.alexey.tabgenerator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO для ответа при аутентификации пользователя (JWT-токен).
 */
@Getter
@AllArgsConstructor
public class AuthResponse {

    private final String token;
}
