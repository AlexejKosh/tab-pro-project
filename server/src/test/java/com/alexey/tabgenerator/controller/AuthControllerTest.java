package com.alexey.tabgenerator.controller;

import com.alexey.tabgenerator.dto.request.LoginRequest;
import com.alexey.tabgenerator.dto.request.NewPasswordRequest;
import com.alexey.tabgenerator.dto.request.RecoverPasswordRequest;
import com.alexey.tabgenerator.dto.request.RegisterRequest;
import com.alexey.tabgenerator.entity.PasswordResetToken;
import com.alexey.tabgenerator.entity.User;
import com.alexey.tabgenerator.repository.PasswordResetTokenRepository;
import com.alexey.tabgenerator.repository.UserRepository;
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
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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
    
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

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
        request.setEmail("testuser1@mail.ru");
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
        request.setUsernameOrEmail("testUser1");
        request.setPassword("qwerty12");

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
        request.setUsernameOrEmail("testuser1@mail.ru");
        request.setPassword("qwerty12");

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
    @DisplayName("Восстановление пароля: отправка email (успех)")
    void recoverPassword_sendEmail_success() throws Exception {
        RecoverPasswordRequest request = new RecoverPasswordRequest();
        request.setEmail("testuser1@mail.ru");

        doNothing().when(emailService)
            .sendPasswordRecovery(anyString(), anyString());

        mockMvc.perform(post("/auth/recover-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists());

        verify(emailService).sendPasswordRecovery(anyString(), anyString());
    }

    @Test
    @DisplayName("Восстановление пароля: email не найден")
    void recoverPassword_emailNotFound() throws Exception {
        RecoverPasswordRequest request = new RecoverPasswordRequest();
        request.setEmail("not_exist@mail.ru");

        mockMvc.perform(post("/auth/recover-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Пользователь с данным email не найден."));
    }

    @Test
    @DisplayName("Проверка токена восстановления: валидный токен (реальный из БД)")
    void checkRecoverPasswordToken_valid() throws Exception {

        String email = "testuser1@mail.ru";

        // 1. Запускаем процесс восстановления
        RecoverPasswordRequest request = new RecoverPasswordRequest();
        request.setEmail(email);

        doNothing().when(emailService)
            .sendPasswordRecovery(anyString(), anyString());

        mockMvc.perform(post("/auth/recover-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // 2. Получаем пользователя
        User user = userRepository.findByEmail(email)
            .orElseThrow();

        // 3. Достаём реальный токен из H2
        PasswordResetToken tokenEntity = passwordResetTokenRepository.findAll()
            .stream()
            .filter(t -> t.getUser().getId().equals(user.getId()))
            .findFirst()
            .orElseThrow();

        String token = tokenEntity.getToken();

        // 4. Проверяем токен через контроллер
        mockMvc.perform(get("/auth/recover-password/{token}", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    @DisplayName("Проверка токена восстановления: невалидный токен")
    void checkRecoverPasswordToken_invalid() throws Exception {

        mockMvc.perform(get("/auth/recover-password/{token}", "invalid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    @DisplayName("Полный сценарий: восстановление пароля через реальный токен")
    void recoverPassword_fullFlow_success() throws Exception {

        String email = "testuser1@mail.ru";

        RecoverPasswordRequest request = new RecoverPasswordRequest();
        request.setEmail(email);

        doNothing().when(emailService)
            .sendPasswordRecovery(anyString(), anyString());

        mockMvc.perform(post("/auth/recover-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        User user = userRepository.findByEmail(email)
            .orElseThrow();

        PasswordResetToken tokenEntity = passwordResetTokenRepository.findAll()
            .stream()
            .filter(t -> t.getUser().getId().equals(user.getId()))
            .findFirst()
            .orElseThrow();

        String token = tokenEntity.getToken();

        mockMvc.perform(get("/auth/recover-password/{token}", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(true));

        NewPasswordRequest newPass = new NewPasswordRequest();
        newPass.setPassword1("newPassword123");
        newPass.setPassword2("newPassword123");

        mockMvc.perform(post("/auth/recover-password/{token}", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPass)))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Сброс пароля: пароли не совпадают")
    void recoverPassword_passwordMismatch() throws Exception {

        String email = "testuser1@mail.ru";

        RecoverPasswordRequest request = new RecoverPasswordRequest();
        request.setEmail(email);

        doNothing().when(emailService)
            .sendPasswordRecovery(anyString(), anyString());

        mockMvc.perform(post("/auth/recover-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        User user = userRepository.findByEmail(email)
            .orElseThrow();

        PasswordResetToken tokenEntity = passwordResetTokenRepository.findAll()
            .stream()
            .filter(t -> t.getUser().getId().equals(user.getId()))
            .findFirst()
            .orElseThrow();

        String token = tokenEntity.getToken();

        mockMvc.perform(get("/auth/recover-password/{token}", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(true));

        NewPasswordRequest newPass = new NewPasswordRequest();
        newPass.setPassword1("password1");
        newPass.setPassword2("password2");

        mockMvc.perform(post("/auth/recover-password/{token}", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}