package com.alexey.tabgenerator.repository;

import com.alexey.tabgenerator.entity.Genre;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты для {@link GenreRepository}.
 *
 * Проверяют работу стандартных методов JPA:
 * - поиск жанров
 * - сохранение нового жанра
 * - удаление жанра
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GenreRepositoryTest {

    @Autowired
    private GenreRepository genreRepository;

    @Test
    @DisplayName("Поиск жанра по id: успех")
    void findById_success() {
        Optional<Genre> genre = genreRepository.findById(1L);

        assertThat(genre).isPresent();
        assertThat(genre.get().getName()).isEqualTo("Blues");
    }

    @Test
    @DisplayName("Поиск жанра по id: несуществующий id")
    void findById_fail_notFound() {
        Optional<Genre> genre = genreRepository.findById(999L);

        assertThat(genre).isEmpty();
    }

    @Test
    @DisplayName("Получение всех жанров: успех")
    void findAll_success() {
        List<Genre> genres = genreRepository.findAll();

        assertThat(genres).hasSize(3);
        assertThat(genres)
            .extracting(Genre::getName)
            .containsExactlyInAnyOrder("Rock", "Blues", "Metal");
    }

    @Test
    @DisplayName("Сохранение нового жанра: успех")
    void save_success() {
        Genre genre = Genre.builder()
            .name("Funk")
            .build();
        Genre savedGenre = genreRepository.save(genre);

        assertThat(savedGenre.getId()).isNotNull();

        Optional<Genre> fromDb = genreRepository.findById(savedGenre.getId());

        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getName()).isEqualTo("Funk");
    }

    @Test
    @DisplayName("Удаление жанра по id: успех")
    void deleteById_success() {
        genreRepository.deleteById(3L);
        Optional<Genre> genre = genreRepository.findById(3L);

        assertThat(genre).isEmpty();
    }
}