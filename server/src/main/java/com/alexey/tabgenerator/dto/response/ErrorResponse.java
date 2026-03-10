package com.alexey.tabgenerator.dto.response;

import lombok.*;

/**
 * DTO для ответа с сообщением об ошибке.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErrorResponse {

    private String message;
}
