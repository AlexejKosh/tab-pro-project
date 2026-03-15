package com.alexey.tabgenerator.service;

import com.alexey.tabgenerator.dto.response.GenreResponse;
import com.alexey.tabgenerator.entity.Genre;
import com.alexey.tabgenerator.exception.NotFoundException;
import com.alexey.tabgenerator.repository.GenreRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для {@link GenreService}
 *
 * Проверяют:
 * - получение жанров
 *
 * Испольюуют моки: {@link GenreRepository}.
 */
class GenreServiceTest {

    // Мок репозитория для сущности Genre,
    // для имитации обращения к БД
    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreService genreService;

    private Genre genre1;
    private Genre genre2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        genre1 = Genre.builder()
            .id(1L)
            .name("Rock")
            .build();

        genre2 = Genre.builder()
            .id(2L)
            .name("Blues")
            .build();
    }

    @Test
    @DisplayName("Получение всех жанров: успех (непустой список)")
    void getAllGenres_success_listOfGenre() {
        when(genreRepository.findAll()).thenReturn(List.of(genre1, genre2));

        List<GenreResponse> result = genreService.getAllGenres();

        assertEquals(2, result.size());
        assertEquals("Rock", result.get(0).getName());
        assertEquals("Blues", result.get(1).getName());
    }

    @Test
    @DisplayName("Получение всех жанров: успех (пустой список)")
    void getAllGenres_success_emptyListOfGenre() {
        when(genreRepository.findAll()).thenReturn(List.of());

        List<GenreResponse> result = genreService.getAllGenres();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Получение жанра по id: успех")
    void getGenreById_success() {
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre1));

        GenreResponse response = genreService.getGenreById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Rock", response.getName());
    }

    @Test
    @DisplayName("Получение жанра по id: несуществующий id")
    void getGenreById_success_notFound() {
        when(genreRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> genreService.getGenreById(99L));
    }
}