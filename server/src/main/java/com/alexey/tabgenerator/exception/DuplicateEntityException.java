package com.alexey.tabgenerator.exception;

/**
 * Исключение, выбрасываемое при попытке создать уже существующий объект.
 */
public class DuplicateEntityException extends RuntimeException {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
