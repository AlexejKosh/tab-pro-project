package com.alexey.tabgenerator.controller;

import com.alexey.tabgenerator.dto.request.LoginRequest;
import com.alexey.tabgenerator.dto.request.RecoverPasswordRequest;
import com.alexey.tabgenerator.dto.request.RegisterRequest;
import com.alexey.tabgenerator.service.EmailService;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты для {@link AuthController}.
 *
 * Проверяют:
 * - регистрацию пользователя
 * - авторизацию пользователя
 * - восстановление пароля по email
 *
 * Используют моки: {@link EmailService}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Мок сервиса с почтой, чтобы
    // не отправлять реальные сообщения на почту
    @MockitoBean
    private EmailService emailService;

    @Test
    @DisplayName("Регистрация: успех")
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("new_user@mail.ru");
        request.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Регистрация: email уже существует")
    void register_fail_emailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("anotherUser");
        request.setEmail("miner_847@mail.ru");
        request.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Регистрация: невалидный email")
    void register_fail_invalidEmailFormat() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("userInvalidEmail");
        request.setEmail("invalid-email-format");
        request.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Регистрация: пароль меньше 6 символов")
    void register_fail_shortPassword() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("userShortPass");
        request.setEmail("shortpass@mail.ru");
        request.setPassword("12345");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Логин: успех (username)")
    void login_success_username() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("alexeyKo");
        request.setPassword("qwerty123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Логин: успех (email)")
    void login_success_email() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("miner_847@mail.ru");
        request.setPassword("qwerty123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Логин: неверные учетные данные")
    void login_fail_invalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("unknownUser");
        request.setPassword("wrongPassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Восстановление пароля: успех")
    void recoverPassword_success() throws Exception {
        RecoverPasswordRequest request = new RecoverPasswordRequest();
        request.setEmail("mail_for_vst@mail.ru");

        doNothing().when(emailService).sendPasswordRecovery(anyString(), anyString());

        mockMvc.perform(post("/auth/recover-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message")
                .value("Новый пароль для входа отправлен на почту."));
    }

    @Test
    @DisplayName("Восстановление пароля: пользователь не найден")
    void recoverPassword_fail_userNotFound() throws Exception {
        RecoverPasswordRequest request = new RecoverPasswordRequest();
        request.setEmail("unknown@email.com");

        mockMvc.perform(post("/auth/recover-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message")
                .value("Пользователь с данным email не найден."));
    }
}