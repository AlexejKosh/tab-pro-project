package com.alexey.tabgenerator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO для ответа при проверка валидности токена восстановления пароля
 */
@Getter
@AllArgsConstructor
public class CheckRecoverPasswordTokenResponse {

    private final Boolean valid;
}
