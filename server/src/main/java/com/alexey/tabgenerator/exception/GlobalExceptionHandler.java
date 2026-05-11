package com.alexey.tabgenerator.exception;

import com.alexey.tabgenerator.dto.response.ErrorResponse;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для приложения.
 * Позволяет централизованно перехватывать ошибки, возникающие
 * в слоях контроллеров, и возвращать клиенту унифицированный ответ
 * с HTTP статусом и сообщением.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // -------------------- Обработка пользовательских исключений --------------------

    /**
     * Обработка NotFoundException (404 Not Found)
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        log.warn("Ошибка 'не найдено': {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Обработка DuplicateEntityException (409 Conflict)
     */
    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEntityException(DuplicateEntityException ex) {
        log.warn("Ошибка 'дублирование сущности': {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    /**
     * Обработка UnauthorizedException (401 Unauthorized)
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("Ошибка 'неавторизованный пользователь': {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Обработка PasswordMismatchException (400 Bad Request)
     */
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatchException(PasswordMismatchException ex) {
        log.warn("Ошибка 'пароли не совпадают': {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка TokenExpiredException (400 Bad Request)
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiredException(TokenExpiredException ex) {
        log.warn("Ошибка 'срок действия токена истёк': {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка ошибок ML сервера (502 Bad Gateway)
     */
    @ExceptionHandler(MlServerException.class)
    public ResponseEntity<ErrorResponse> handleMlServerException(MlServerException ex) {
        log.error("Ошибка ML сервера: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), ex.getStatus());
    }

    /**
     * Обработка ошибок отправки email (502 Bad Gateway)
     */
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendException(EmailSendException ex) {
        log.error("Ошибка 'отправка письма': {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_GATEWAY);
    }

    /**
     * Обработка ForbiddenException (403 Forbidden)
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex) {
        log.warn("Ошибка 'запрещено': {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    /**
     * Обработка InvalidJwtTokenException (401 Unauthorized)
     */
    @ExceptionHandler(InvalidJwtTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJwtTokenException(
        InvalidJwtTokenException ex
    ) {
        log.warn("Ошибка проверки JWT: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Обработка TooManyGenerateRequestsException (429 Unauthorized)
     */
    @ExceptionHandler(TooManyGenerateRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyGenerateRequestsException(
        TooManyGenerateRequestsException ex
    ) {
        log.warn("Ошибка 'слишком много запросов для генерации с одного IP': {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Обработка FileStorageException (500 Internal Server Error)
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(FileStorageException ex) {
        log.error("Ошибка работы с файловым хранилищем: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // -------------------- Обработка стандартных исключений Jakarta / Spring --------------------

    /**
     * Валидационные ошибки (400 Bad Request)
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        log.warn("Ошибка 'валидация': {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Ошибки валидации аргументов (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex
    ) {
        String details = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        log.warn("Ошибка 'аргумент не прошёл валидацию': {}", details);
        return buildResponse(details, HttpStatus.BAD_REQUEST);
    }

    /**
     * Ресурс не найден (404 Not Found)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("Ошибка 'ресурс не найден': {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Метод HTTP не разрешён (405 Method Not Allowed)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Ошибка 'метод не разрешён': {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Некорректный запрос (400 Method Not Allowed)
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        log.warn("Ошибка 'некорректный запрос': {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка всех непредвиденных ошибок (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Непредвиденная ошибка: {}", ex.getMessage(), ex);
        return buildResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // -------------------- Вспомогательные методы --------------------

    /**
     * Формирует ResponseEntity с ErrorResponse и нужным HTTP статусом.
     */
    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(new ErrorResponse(message), status);
    }
}
