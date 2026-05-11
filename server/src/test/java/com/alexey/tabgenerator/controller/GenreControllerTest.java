package com.alexey.tabgenerator.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для {@link GenreController}.
 *
 * Проверяют:
 * - получение жанров
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Получение всех жанров: успех")
    void getAllGenres_success() throws Exception {

        mockMvc.perform(get("/genres")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").exists())
            .andExpect(jsonPath("$[1].name").exists())
            .andExpect(jsonPath("$[2].name").exists());
    }

    @Test
    @DisplayName("Получение жанра по id: успех")
    void getGenreById_success() throws Exception {

        mockMvc.perform(get("/genres/2")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Metal"));
    }

    @Test
    @DisplayName("Получение жанра по id: несуществующий id")
    void getGenreById_fail_notFound() throws Exception {

        mockMvc.perform(get("/genres/10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}