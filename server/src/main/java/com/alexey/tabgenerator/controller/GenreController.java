package com.alexey.tabgenerator.controller;

import com.alexey.tabgenerator.dto.response.GenreResponse;
import com.alexey.tabgenerator.service.GenreService;

import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<List<GenreResponse>> getAllGenres(
        HttpServletRequest httpRequest
    ) {

        log.debug("[{}] Запрос на получение списка всех жанров",
            httpRequest.getRemoteAddr());

        List<GenreResponse> genres = genreService.getAllGenres();

        return ResponseEntity.ok(genres);
    }

    /**
     * Получение информации о жанре по его идентификатору.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenreResponse> getGenreById(
        @PathVariable Long id, HttpServletRequest httpRequest
    ) {

        log.debug("[{}] Запрос на получение жанра: id={}",
            httpRequest.getRemoteAddr(), id);

        GenreResponse genre = genreService.getGenreById(id);

        return ResponseEntity.ok(genre);
    }
}