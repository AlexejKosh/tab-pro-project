package com.alexey.tabgenerator.exception;

/**
 * Исключение, выбрасываемое при отсутствии ресурса в проекте.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}