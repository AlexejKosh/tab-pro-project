package com.alexey.tabgenerator.exception;

/**
 * Исключение, выбрасываемое при попытке работы с некорректным JWT токеном.
 */
public class InvalidJwtTokenException extends RuntimeException {
    public InvalidJwtTokenException(String message) {
        super(message);
    }
}