package com.alexey.tabgenerator.controller;

import com.alexey.tabgenerator.dto.response.GenreResponse;
import com.alexey.tabgenerator.service.GenreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для работы с музыкальными жанрами.
 * Обрабатывает запросы получения списка жанров
 * и информации о конкретном жанре.
 */
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
@Slf4j
public class GenreController {

    private final GenreService genreService;

    /**
     * Получение списка всех доступных жанров.
     */
    @GetMapping
    public ResponseEntity<List<GenreResponse>> getAllGenres() {

        log.debug("Запрос на получение списка всех жанров");

        List<GenreResponse> genres = genreService.getAllGenres();

        log.info("Запрос на получение списка жанров успешно обработан: size={}", genres.size());

        return ResponseEntity.ok(genres);
    }

    /**
     * Получение информации о жанре по его идентификатору.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenreResponse> getGenreById(@PathVariable Long id) {

        log.debug("Запрос на получение жанра: id={}", id);

        GenreResponse genre = genreService.getGenreById(id);

        log.info("Запрос на получение жанра успешно обработан: name={}", genre.getName());

        return ResponseEntity.ok(genre);
    }
}