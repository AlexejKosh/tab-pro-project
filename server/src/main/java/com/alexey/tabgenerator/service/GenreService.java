package com.alexey.tabgenerator.service;

import com.alexey.tabgenerator.dto.response.GenreResponse;
import com.alexey.tabgenerator.entity.Genre;
import com.alexey.tabgenerator.exception.NotFoundException;
import com.alexey.tabgenerator.repository.GenreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с музыкальными жанрами.
 * Предоставляет методы получения всех жанров и конкретного жанра по ID.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    /**
     * Получение списка всех жанров.
     */
    @Transactional(readOnly = true)
    public List<GenreResponse> getAllGenres() {

        List<Genre> genres = genreRepository.findAll();

        log.debug("Получен список жанров: size={}", genres.size());

        // Преобразование сущностей в DTO для ответа
        return genres.stream()
                .map(GenreResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Получение жанра по идентификатору.
     */
    @Transactional(readOnly = true)
    public GenreResponse getGenreById(Long id) {

        log.debug("Получение жанра: id={}", id);

        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> {
                    return new NotFoundException("Жанр не найден");
                });

        log.debug(
            "Получен жанр: id={}, name={}",
            genre.getId(),
            genre.getName()
        );

        return GenreResponse.fromEntity(genre);
    }
}