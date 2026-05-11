package com.alexey.tabgenerator.service;

import com.alexey.tabgenerator.dto.request.GenerateTabRequest;
import com.alexey.tabgenerator.dto.request.SaveTabRequest;
import com.alexey.tabgenerator.dto.response.GenerateResponse;
import com.alexey.tabgenerator.dto.response.MlServerGenerateResponse;
import com.alexey.tabgenerator.dto.response.TabResponse;
import com.alexey.tabgenerator.dto.response.TabSummaryResponse;
import com.alexey.tabgenerator.entity.Genre;
import com.alexey.tabgenerator.entity.Tab;
import com.alexey.tabgenerator.entity.User;
import com.alexey.tabgenerator.exception.FileStorageException;
import com.alexey.tabgenerator.exception.ForbiddenException;
import com.alexey.tabgenerator.exception.NotFoundException;
import com.alexey.tabgenerator.repository.GenreRepository;
import com.alexey.tabgenerator.repository.TabRepository;
import com.alexey.tabgenerator.integration.MlClient;
import com.alexey.tabgenerator.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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

    @Value("${file.upload-dir}")
    private String uploadDirPath;

    /**
     * Получение всех табулатур текущего пользователя.
     */
    @Transactional(readOnly = true)
    public List<TabSummaryResponse> getAllTabs() {

        User user = securityUtils.getCurrentUser();

        log.debug(
            "Получение списка табулатур пользователя: userId={}, username={}",
            user.getId(),
            user.getUsername()
        );

        List<Tab> tabs = tabRepository.findByUser(user);

        return tabs.stream()
            .map(TabSummaryResponse::fromEntity)
            .toList();
    }

    /**
     * Получение конкретной табулатуры по ID.
     */
    @Transactional(readOnly = true)
    public TabResponse getTabById(Long id) {

        User user = securityUtils.getCurrentUser();

        log.debug(
            "Получение табулатуры: tabId={}, userId={}",
            id,
            user.getId()
        );

        Tab tab = tabRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Табулатура не найдена"));

        if (!tab.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Нет доступа к данной табулатуре");
        }

        String audioData;

        try {
            Path path = Paths.get(uploadDirPath)
                .resolve(tab.getAudioUrl());

            audioData = Files.readString(path);

        } catch (IOException e) {
            throw new FileStorageException("Ошибка чтения аудио файла: " +
                tab.getAudioUrl() + ", детали: " + e.toString());
        }

        log.info(
            "Табулатура успешно получена: tabId={}, userId={}",
            id, user.getId()
        );

        return TabResponse.builder()
            .id(tab.getId())
            .genreId(tab.getGenre().getId())
            .title(tab.getTitle())
            .signature(tab.getSignature())
            .musicKey(tab.getMusicKey())
            .bpm(tab.getBpm())
            .chordProgression(tab.getChordProgression())
            .tabData(tab.getTabData())
            .audioData(audioData)
            .createdAt(tab.getCreatedAt())
            .build();
    }

    /**
     * Генерация табулатуры через ML сервер.
     */
    @Transactional(readOnly = true)
    public GenerateResponse generateTab(GenerateTabRequest request) {

        log.debug(
            "Генерация табулатуры: genreId={}, bpm={}, key={}",
            request.getGenreId(),
            request.getBpm(),
            request.getMusicKey()
        );

        Genre genre = genreRepository.findById(request.getGenreId())
            .orElseThrow(() -> {
                return new NotFoundException("Жанр не найден");
            });

        MlServerGenerateResponse generatedTab = mlClient.generateTab(request);

        log.info(
            "Табулатура сгенерирована: genreId={}", genre.getId()
        );

        return GenerateResponse.builder()
            .genreId(genre.getId())
            .signature(request.getSignature())
            .musicKey(request.getMusicKey())
            .bpm(request.getBpm())
            .chordProgression(request.getChordProgression())
            .tabData(generatedTab.getTabData())
            .audioData(generatedTab.getAudioData())
            .build();
    }

    /**
     * Сохранение табулатуры для текущего пользователя.
     */
    @Transactional
    public void saveTab(SaveTabRequest request) {

        User user = securityUtils.getCurrentUser();

        log.debug(
            "Сохранение табулатуры: title={}, genreId={}",
            request.getTitle(),
            request.getGenreId()
        );

        Genre genre = genreRepository.findById(request.getGenreId())
            .orElseThrow(() -> new NotFoundException("Жанр не найден"));

        String filename = UUID.randomUUID() + ".b64";

        Path uploadDir = Paths.get(uploadDirPath);
        Path filePath = uploadDir.resolve(filename);

        try {
            Files.createDirectories(uploadDir);

            Files.writeString(filePath, request.getAudioData());

        } catch (IOException e) {
            throw new FileStorageException("Ошибка сохранения аудио файла:" +
                filename + ", детали: " + e.toString());
        }

        Tab tab = Tab.builder()
            .user(user)
            .genre(genre)
            .title(request.getTitle())
            .chordProgression(request.getChordProgression())
            .musicKey(request.getMusicKey())
            .signature(request.getSignature())
            .bpm(request.getBpm())
            .tabData(request.getTabData())
            .audioUrl(filename)
            .createdAt(OffsetDateTime.now())
            .build();

        tabRepository.save(tab);

        log.info(
            "Табулатура сохранена: tabId={}, title={}, userId={}",
            tab.getId(),
            tab.getTitle(),
            user.getId()
        );
    }

    /**
     * Удаление табулатуры для текущего пользователя.
     */
    @Transactional
    public void deleteTab(Long id) {

        User user = securityUtils.getCurrentUser();

        log.debug(
            "Попытка удаленить табулатуру: tabId={}, userId={}",
            id, user.getId()
        );

        Tab tab = tabRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Табулатура не найдена"));

        if (!tab.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Нет доступа к данной табулатуре");
        }

        try {
            Path filePath = Paths.get(uploadDirPath)
                .resolve(tab.getAudioUrl());

            Files.deleteIfExists(filePath);

        } catch (IOException e) {
            throw new FileStorageException("Ошибка удаления аудио файла: " +
                tab.getAudioUrl() + ", детали: " + e.toString());
        }

        log.warn(
            "Табулатура удалена: tabId={}, userId={}",
            id, user.getId()
        );

        tabRepository.delete(tab);
    }
}