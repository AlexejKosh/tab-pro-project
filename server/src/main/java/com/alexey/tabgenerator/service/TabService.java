package com.alexey.tabgenerator.service;

import com.alexey.tabgenerator.dto.request.GenerateTabRequest;
import com.alexey.tabgenerator.dto.request.SaveTabRequest;
import com.alexey.tabgenerator.dto.response.GenerateResponse;
import com.alexey.tabgenerator.dto.response.TabResponse;
import com.alexey.tabgenerator.entity.Genre;
import com.alexey.tabgenerator.entity.Tab;
import com.alexey.tabgenerator.entity.User;
import com.alexey.tabgenerator.exception.ForbiddenException;
import com.alexey.tabgenerator.exception.NotFoundException;
import com.alexey.tabgenerator.repository.GenreRepository;
import com.alexey.tabgenerator.repository.TabRepository;
import com.alexey.tabgenerator.integration.MlClient;
import com.alexey.tabgenerator.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Сервис для работы с табулатурами.
 * Обеспечивает получение, генерацию, сохранение и удаление табулатур для текущего пользователя.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TabService {

    private final TabRepository tabRepository;
    private final GenreRepository genreRepository;
    private final MlClient mlClient;
    private final SecurityUtils securityUtils;

    /**
     * Получение всех табулатур текущего пользователя.
     */
    @Transactional(readOnly = true)
    public List<TabResponse> getAllTabs() {

        log.debug("Получение всех табулатур текущего пользователя");

        User user = securityUtils.getCurrentUser();

        List<Tab> tabs = tabRepository.findByUser(user);

        log.info("Все табулатуры пользователя успешно получены: size={}, username={}",
            tabs.size(), user.getUsername());

        return tabs.stream()
            .map(TabResponse::fromEntity)
            .toList();
    }

    /**
     * Получение конкретной табулатуры по ID.
     */
    @Transactional(readOnly = true)
    public TabResponse getTabById(Long id) {

        log.debug("Получение табулатуры: id={}", id);

        User user = securityUtils.getCurrentUser();

        Tab tab = tabRepository.findById(id)
            .orElseThrow(() -> {
                return new NotFoundException("Табулатура не найдена");
            });

        if (!tab.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Нет доступа к данной табулатуре");
        }

        log.info("Табулатура успешно получена: id={}, username={}", id, user.getUsername());

        return TabResponse.fromEntity(tab);
    }

    /**
     * Генерация табулатуры через ML сервер.
     */
    @Transactional(readOnly = true)
    public GenerateResponse generateTab(GenerateTabRequest request) {

        log.debug("Запрос на генерацию табулатуры: title={}, genreId={}, signature={}",
            request.getTitle(), request.getGenreId(), request.getSignature());

        Genre genre = genreRepository.findById(request.getGenreId())
            .orElseThrow(() -> {
                return new NotFoundException("Жанр не найден");
            });

        List<List<Integer>> generatedTab =
            mlClient.generateTab(
                request.getTitle(),
                request.getChordProgression(),
                request.getSignature(),
                genre.getId()
            );

        log.info("Табулатура успешно сгенерирована через ML сервер");

        return GenerateResponse.builder()
            .genreId(genre.getId())
            .title(request.getTitle())
            .signature(request.getSignature())
            .chordProgression(request.getChordProgression())
            .tabData(generatedTab)
            .build();
    }

    /**
     * Сохранение табулатуры для текущего пользователя.
     */
    @Transactional
    public void saveTab(SaveTabRequest request) {

        User user = securityUtils.getCurrentUser();

        log.debug("Сохранение табулатуры: title={}, username={}", request.getTitle(), user.getUsername());

        Genre genre = genreRepository.findById(request.getGenreId())
            .orElseThrow(() -> {
                return new NotFoundException("Жанр не найден");
            });

        Tab tab = Tab.builder()
            .user(user)
            .genre(genre)
            .title(request.getTitle())
            .signature(request.getSignature())
            .chordProgression(request.getChordProgression())
            .tabData(request.getTabData())
            .createdAt(OffsetDateTime.now())
            .build();

        tabRepository.save(tab);

        log.info("Табулатура успешно сохранена: id={}, title={}, username={}",
            tab.getId(), tab.getTitle(), user.getUsername());
    }

    /**
     * Удаление табулатуры для текущего пользователя.
     */
    @Transactional
    public void deleteTab(Long id) {

        User user = securityUtils.getCurrentUser();

        log.debug("Удаление табулатуры: id={}, username={}", id, user.getUsername());

        Tab tab = tabRepository.findById(id)
            .orElseThrow(() -> {
                return new NotFoundException("Табулатура не найдена");
            });

        if (!tab.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Нет доступа к данной табулатуре");
        }

        tabRepository.delete(tab);

        log.info("Табулатура успешно удалена: id={}, username={}", id, user.getUsername());
    }
}