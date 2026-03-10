package com.alexey.tabgenerator.exception;

/**
 * Исключение, выбрасываемое при отсутствии доступа.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}