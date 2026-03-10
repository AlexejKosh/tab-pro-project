package com.alexey.tabgenerator.exception;

/**
 * Исключение, выбрасываемое при ошибке преобразования JSON.
 */
public class JsonConversionException extends RuntimeException {
    public JsonConversionException(String message) {
        super(message);
    }
}