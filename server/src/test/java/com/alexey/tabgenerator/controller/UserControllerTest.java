package com.alexey.tabgenerator.controller;

import com.alexey.tabgenerator.dto.request.ChangePasswordRequest;
import com.alexey.tabgenerator.security.JwtService;
import com.alexey.tabgenerator.security.UserDetailsServiceImpl;

import com.alexey.tabgenerator.service.TabService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.transaction.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для {@link UserController}.
 *
 * Проверяют:
 * - получение текущего пользователя
 * - смену пароля для текущего пользователя
 * - удаление текущего пользователя
 *
 * Используют моки: {@link JwtService},
 * {@link UserDetailsServiceImpl}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Мок компонентов безопасности
    // для иммитации авторизованного пользователя
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private TabService tabService;

    private static final String TOKEN = "mock-token";

    @BeforeEach
    void setup() {
        UserDetails userDetails = User
            .withUsername("testUser1")
            .password("encoded")
            .authorities("ROLE_USER")
            .build();

        when(jwtService.extractUsername(TOKEN))
            .thenReturn("testUser1");
        when(jwtService.isTokenValid(anyString(), any()))
            .thenReturn(true);
        when(jwtService.buildAuthentication(userDetails))
            .thenCallRealMethod();
        when(userDetailsService.loadUserByUsername("testUser1"))
            .thenReturn(userDetails);
    }

    @Test
    @DisplayName("Получение текущего пользователя: успех")
    void getCurrentUser_success() throws Exception {

        mockMvc.perform(get("/users/me")
                .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testUser1"))
            .andExpect(jsonPath("$.email").value("testuser1@mail.ru"));
    }

    @Test
    @DisplayName("Получение текущего пользователя: пользователь не авторизован")
    void getCurrentUser_fail_unauthorized() throws Exception {

        mockMvc.perform(get("/users/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Смена пароля: успех")
    void changePassword_success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("qwerty12");
        request.setNewPassword("newPassword123");

        mockMvc.perform(put("/users/password")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Смена пароля: старый пароль неверный")
    void changePassword_fail_wrongOldPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongPassword");
        request.setNewPassword("newPassword123");

        mockMvc.perform(put("/users/password")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Смена пароля: пользователь не авторизован")
    void changePassword_fail_unauthorized() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("qwerty123");
        request.setNewPassword("newPassword123");

        mockMvc.perform(put("/users/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Удаление текущего пользователя: успех")
    void deleteCurrentUser_success() throws Exception {

        mockMvc.perform(delete("/users/me")
                .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Удаление текущего пользователя: пользователь не авторизован")
    void deleteCurrentUser_fail_unauthorized() throws Exception {

        mockMvc.perform(delete("/users/me"))
            .andExpect(status().isUnauthorized());
    }
}