package com.alexey.tabgenerator.exception;

/**
 * Исключение, выбрасываемое при неавторизованном доступе.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}