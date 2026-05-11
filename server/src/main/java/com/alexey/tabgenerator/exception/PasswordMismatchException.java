package com.alexey.tabgenerator.exception;

/**
 * Исключение, выбрасываемое при не соответсвии паролей,
 * введённых пользователем
 */
public class PasswordMismatchException extends RuntimeException {
  public PasswordMismatchException(String message) {
    super(message);
  }
}
