package com.alexey.tabgenerator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO для ответа при восстановлении пароля.
 */
@Getter
@AllArgsConstructor
public class RecoverPasswordResponse {

    private String message;
}