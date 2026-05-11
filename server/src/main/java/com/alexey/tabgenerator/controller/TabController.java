package com.alexey.tabgenerator.controller;

import com.alexey.tabgenerator.dto.request.SaveTabRequest;
import com.alexey.tabgenerator.dto.request.GenerateTabRequest;
import com.alexey.tabgenerator.dto.response.GenerateResponse;
import com.alexey.tabgenerator.dto.response.TabResponse;
import com.alexey.tabgenerator.dto.response.TabSummaryResponse;
import com.alexey.tabgenerator.exception.TooManyGenerateRequestsException;
import com.alexey.tabgenerator.service.GenerationLockService;
import com.alexey.tabgenerator.service.TabService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для работы с гитарными табулатурами пользователя.
 * Обрабатывает запросы получения, генерации, сохранения
 * и удаления табулатур.
 */
@RestController
@RequestMapping("/tabs")
@RequiredArgsConstructor
@Slf4j
public class TabController {

    private final TabService tabService;
    private final GenerationLockService generationLockService;

    /**
     * Получение всех табулатур текущего пользователя.
     */
    @GetMapping
    public ResponseEntity<List<TabSummaryResponse>> getAllTabs(
        HttpServletRequest httpRequest
    ) {

        log.debug("[{}] Запрос на получение всех табулатур текущего пользователя",
            httpRequest.getRemoteAddr());

        List<TabSummaryResponse> tabs = tabService.getAllTabs();

        return ResponseEntity.ok(tabs);
    }

    /**
     * Получение табулатуры по её идентификатору.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TabResponse> getTabById(
        @PathVariable Long id, HttpServletRequest httpRequest
    ) {

        log.debug("[{}] Запрос на получение табулатуры: id={}",
            httpRequest.getRemoteAddr(), id);

        TabResponse tab = tabService.getTabById(id);

        return ResponseEntity.ok(tab);
    }

    /**
     * Генерация новой табулатуры с использованием ML-сервера.
     */
    @PostMapping("/generate")
    public ResponseEntity<GenerateResponse> generateTab(
        HttpServletRequest httpRequest,
        @Valid @RequestBody GenerateTabRequest request
    ) {

        String ip = httpRequest.getRemoteAddr();

        log.debug("[{}] Запрос на генерацию табулатуры: genreId={}, musicKey={}, bpm={}",
            ip, request.getGenreId(), request.getMusicKey(), request.getBpm());

        if (!generationLockService.tryLock(ip)) {
            throw new TooManyGenerateRequestsException("Генерация уже в процессе для этого IP");
        }

        try {
            request.setIp(ip);
            GenerateResponse tab = tabService.generateTab(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(tab);
        } finally {
            generationLockService.unlock(ip);
        }
    }

    /**
     * Сохранение табулатуры пользователя в базе данных.
     */
    @PostMapping
    public ResponseEntity<Void> saveTab(
        @Valid @RequestBody SaveTabRequest request,
        HttpServletRequest httpRequest
    ) {

        log.debug("[{}] Запрос на сохранение табулатуры: title={}, genreId={}",
            httpRequest.getRemoteAddr(), request.getTitle(), request.getGenreId());

        tabService.saveTab(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Удаление табулатуры пользователя по её идентификатору.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTab(
        @PathVariable Long id,
        HttpServletRequest httpRequest
    ) {

        log.debug("[{}] Запрос на удаление табулатуры: id={}",
            httpRequest.getRemoteAddr(), id);

        tabService.deleteTab(id);

        return ResponseEntity.noContent().build();
    }
}