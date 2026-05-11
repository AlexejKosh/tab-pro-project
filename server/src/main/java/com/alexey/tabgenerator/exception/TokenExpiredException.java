package com.alexey.tabgenerator.exception;

/**
 * Исключение, выбрасываемое при истечении действия токена
 * для восстановления пароля пользователя
 */
public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
