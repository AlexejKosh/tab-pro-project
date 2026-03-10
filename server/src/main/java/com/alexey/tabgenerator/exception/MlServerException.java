package com.alexey.tabgenerator.exception;

/**
 * Исключение, выбрасываемое при ошибке работы ML сервера.
 */
public class MlServerException extends RuntimeException {
    public MlServerException(String message) {
        super(message);
    }
}