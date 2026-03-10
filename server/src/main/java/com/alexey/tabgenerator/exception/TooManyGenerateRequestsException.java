package com.alexey.tabgenerator.exception;

/**
 * Исключение, выбрасываемое при отправке нового запроса о генерации табулатуры
 * в то время, как уже обрабатывается процесс генерации для данного IP.
 */
public class TooManyGenerateRequestsException extends RuntimeException {
    public TooManyGenerateRequestsException(String message) {
        super(message);
    }
}
