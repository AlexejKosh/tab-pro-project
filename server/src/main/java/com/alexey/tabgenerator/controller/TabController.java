package com.alexey.tabgenerator.controller;

import com.alexey.tabgenerator.dto.request.SaveTabRequest;
import com.alexey.tabgenerator.dto.request.GenerateTabRequest;
import com.alexey.tabgenerator.dto.response.GenerateResponse;
import com.alexey.tabgenerator.dto.response.TabResponse;
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
    public ResponseEntity<List<TabResponse>> getAllTabs() {

        log.debug("Запрос на получение всех табулатур текущего пользователя");

        List<TabResponse> tabs = tabService.getAllTabs();

        log.info("Запрос на получение всех табулатур текущего пользователя успешно обработан: size={}",
            tabs.size());

        return ResponseEntity.ok(tabs);
    }

    /**
     * Получение табулатуры по её идентификатору.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TabResponse> getTabById(@PathVariable Long id) {

        log.debug("Запрос на получение табулатуры: id={}", id);

        TabResponse tab = tabService.getTabById(id);

        log.info("Запрос на получение табулатуры успешно обработан: id={}, title={}", id, tab.getTitle());

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

        log.debug("Запрос на генерацию табулатуры: title={}, genreId={}",
            request.getTitle(), request.getGenreId());

        String ip = httpRequest.getRemoteAddr();

        if (!generationLockService.tryLock(ip)) {
            throw new TooManyGenerateRequestsException("Генерация уже в процессе для этого IP");
        }

        try {
            GenerateResponse tab = tabService.generateTab(request);

            log.info("Запрос на генерацию табулатуры успешно обработан: title={}, genreId={}",
                tab.getTitle(), tab.getGenreId());

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
        @Valid @RequestBody SaveTabRequest request
    ) {

        log.debug("Запрос на сохранение табулатуры: title={}, genreId={}",
            request.getTitle(), request.getGenreId());

        tabService.saveTab(request);

        log.info("Запрос на сохранение табулатуры успешно обработан: title={}",
            request.getTitle());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Удаление табулатуры пользователя по её идентификатору.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTab(@PathVariable Long id) {

        log.debug("Запрос на удаление табулатуры: id={}", id);

        tabService.deleteTab(id);

        log.warn("Запрос на удаление табулатуры успешно обработан: id={}", id);

        return ResponseEntity.noContent().build();
    }
}