package com.alexey.tabgenerator.exception;

/**
 * Исключение, выбрасываемое при ошибке отправки email.
 */
public class EmailSendException extends RuntimeException {
    public EmailSendException(String message) {
      super(message);
    }
}