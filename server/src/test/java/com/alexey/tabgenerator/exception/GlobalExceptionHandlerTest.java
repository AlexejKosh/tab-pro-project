package com.alexey.tabgenerator.exception;

import com.alexey.tabgenerator.dto.response.ErrorResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.validation.ValidationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для {@link GlobalExceptionHandler}.
 *
 * Проверяют:
 * - обработку пользовательских исключения
 * - обработку стандартных исключений Jakarta / Spring
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Обработка NotFound: объект отсутствует в БД")
    void handleNotFoundException_notFound() {
        NotFoundException ex = new NotFoundException("Объект отсутствует в БД");
        ResponseEntity<?> resp = handler.handleNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertEquals("Объект отсутствует в БД", ((ErrorResponse)resp.getBody()).getMessage());
    }

    @Test
    @DisplayName("Обработка DuplicateEntity: дублирование сущности")
    void handleDuplicateEntityException_conflict() {
        DuplicateEntityException ex = new DuplicateEntityException("Сущность уже существует");
        ResponseEntity<?> resp = handler.handleDuplicateEntityException(ex);

        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertEquals("Сущность уже существует", ((ErrorResponse)resp.getBody()).getMessage());
    }

    @Test
    @DisplayName("Обработка Unauthorized: доступ без авторизации")
    void handleUnauthorizedException_unauthorized() {
        UnauthorizedException ex = new UnauthorizedException("Не авторизован");
        ResponseEntity<?> resp = handler.handleUnauthorizedException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertEquals("Не авторизован", ((ErrorResponse)resp.getBody()).getMessage());
    }

    @Test
    @DisplayName("Обработка MlServer: ошибка ML сервера")
    void handleMlServerException_badGateway() {
        MlServerException ex = new MlServerException("Ошибка ML сервера");
        ResponseEntity<?> resp = handler.handleMlServerException(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, resp.getStatusCode());
        assertEquals("Ошибка ML сервера", ((ErrorResponse)resp.getBody()).getMessage());
    }

    @Test
    @DisplayName("Обработка EmailSend: ошибка отправки письма")
    void handleEmailSendException_badGateway() {
        EmailSendException ex = new EmailSendException("Не удалось отправить письмо");
        ResponseEntity<?> resp = handler.handleEmailSendException(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, resp.getStatusCode());
        assertEquals("Не удалось отправить письмо", ((ErrorResponse)resp.getBody()).getMessage());
    }

    @Test
    @DisplayName("Обработка Forbidden: доступ запрещен")
    void handleForbiddenException_forbidden() {
        ForbiddenException ex = new ForbiddenException("Доступ к ресурсу запрещен");
        ResponseEntity<?> resp = handler.handleForbiddenException(ex);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        assertEquals("Доступ к ресурсу запрещен", ((ErrorResponse)resp.getBody()).getMessage());
    }

    @Test
    @DisplayName("Обработка JsonConversion: ошибка обработки JSON")
    void handleJsonConversionException_internalServerError() {
        JsonConversionException ex = new JsonConversionException("Ошибка обработки JSON");
        ResponseEntity<?> resp = handler.handleJsonConversionException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("Ошибка обработки JSON", ((ErrorResponse)resp.getBody()).getMessage());
    }

    @Test
    @DisplayName("Обработка InvalidJwtToken: недействительный JWT")
    void handleInvalidJwtTokenException_unauthorized() {
        InvalidJwtTokenException ex = new InvalidJwtTokenException("Неверный токен");
        ResponseEntity<?> resp = handler.handleInvalidJwtTokenException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertEquals("Неверный токен", ((ErrorResponse)resp.getBody()).getMessage());
    }

    @Test
    @DisplayName("Обработка TooManyGenerateRequests: много запросов на генерацию")
    void handleTooManyGenerateRequestsException_tooManyRequests() {
        TooManyGenerateRequestsException ex = new TooManyGenerateRequestsException("Слишком много запросов");
        ResponseEntity<?> resp = handler.handleTooManyGenerateRequestsException(ex);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, resp.getStatusCode());
        assertEquals("Слишком много запросов", ((ErrorResponse)resp.getBody()).getMessage());
    }

    @Test
    @DisplayName("Обработка Validation: ошибка валидации")
    void handleValidationException_badRequest() {
        ValidationException ex = new ValidationException("Некорректные данные");
        ResponseEntity<?> resp = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Некорректные данные", ((ErrorResponse)resp.getBody()).getMessage());
    }

    @Test
    @DisplayName("Обработка MethodArgumentNotValid: ошибка аргументов метода")
    void handleMethodArgumentNotValidException_badRequest() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);

        FieldError fe = new FieldError("object","field","не должно быть пустым");

        when(bindingResult.getFieldErrors()).thenReturn(List.of(fe));

        ResponseEntity<?> resp = handler.handleMethodArgumentNotValidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(((ErrorResponse)resp.getBody()).getMessage().
            contains("field: не должно быть пустым"));
    }

    @Test
    @DisplayName("Обработка NoResourceFound: ресурс не найден")
    void handleNoResourceFoundException_notFound() {
        HttpMethod method = HttpMethod.GET;
        String requestUri = "/some/request";
        String resourcePath = "/missing/resource";
        NoResourceFoundException ex = new NoResourceFoundException(method, requestUri, resourcePath);
        ResponseEntity<?> resp = handler.handleNoResourceFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertTrue(((ErrorResponse)resp.getBody()).getMessage().contains(resourcePath));
    }

    @Test
    @DisplayName("Обработка HttpRequestMethodNotSupported: метод не поддерживается")
    void handleMethodNotSupported_methodNotAllowed() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");
        ResponseEntity<?> resp = handler.handleMethodNotSupported(ex);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, resp.getStatusCode());
        assertEquals("Request method 'POST' is not supported",
            ((ErrorResponse)resp.getBody()).getMessage());
    }

    @Test
    @DisplayName("Обработка Exception: неизвестная ошибка")
    void handleGenericException_internalServerError() {
        Exception ex = new Exception("Что-то пошло не так");
        ResponseEntity<?> resp = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("Что-то пошло не так", ((ErrorResponse)resp.getBody()).getMessage());
    }
}