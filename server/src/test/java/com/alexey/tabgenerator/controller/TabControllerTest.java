package com.alexey.tabgenerator.controller;

import com.alexey.tabgenerator.dto.request.GenerateTabRequest;
import com.alexey.tabgenerator.dto.request.SaveTabRequest;
import com.alexey.tabgenerator.dto.response.MlServerGenerateResponse;
import com.alexey.tabgenerator.entity.Tab;
import com.alexey.tabgenerator.integration.MlClient;
import com.alexey.tabgenerator.repository.TabRepository;
import com.alexey.tabgenerator.repository.GenreRepository;
import com.alexey.tabgenerator.repository.UserRepository;
import com.alexey.tabgenerator.security.JwtService;
import com.alexey.tabgenerator.security.SecurityUtils;
import com.alexey.tabgenerator.security.UserDetailsServiceImpl;
import com.alexey.tabgenerator.service.GenerationLockService;

import jakarta.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для {@link TabController}.
 *
 * Проверяют:
 * - получение табулатур
 * - обработку запроса на генерацию табулатур
 * - сохранение табулатур
 * - удаление табулатур
 *
 * Используют моки: {@link MlClient},
 * {@link GenerationLockService}, {@link JwtService},
 * {@link UserDetailsServiceImpl}, {@link SecurityUtils}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TabControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TabRepository tabRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GenreRepository genreRepository;

    // Мок ML клиента, чтобы не выполнять реальные HTTP-запросы к ML серверу
    @MockitoBean
    private MlClient mlClient;

    // Мок сервиса блокировки частых запросов с одного IP
    @MockitoBean
    private GenerationLockService generationLockService;

    // Мок компонентов безопасности для имитации авторизованного пользователя
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private SecurityUtils securityUtils;

    private static final String TOKEN = "mock-token";

    private com.alexey.tabgenerator.entity.User currentUser;

    @BeforeEach
    void setup() {
        currentUser = userRepository.findById(1L).orElseThrow();

        UserDetails userDetails = User
            .withUsername(currentUser.getUsername())
            .password("encoded")
            .authorities("ROLE_USER")
            .build();

        when(jwtService.extractUsername(TOKEN)).thenReturn(currentUser.getUsername());
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(true);
        when(jwtService.buildAuthentication(userDetails)).thenCallRealMethod();
        when(userDetailsService.loadUserByUsername(currentUser.getUsername())).thenReturn(userDetails);
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    @DisplayName("Получение табулатур: успех")
    void getAllTabs_success() throws Exception {
        mockMvc.perform(get("/tabs")
            .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].title").value("Что-то вроде Битлз"));
    }

    @Test
    @DisplayName("Получение табулатур: пользователь не авторизован")
    void getAllTabs_fail_unauthorized() throws Exception {

        mockMvc.perform(get("/tabs"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Получение табулатуры по id: успех")
    void getTabById_success() throws Exception {
        mockMvc.perform(get("/tabs/1")
            .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Что-то вроде Битлз"));
    }

    @Test
    @DisplayName("Получение табулатуры по id: доступ запрещён")
    void getTabById_fail_forbidden() throws Exception {
        mockMvc.perform(get("/tabs/4")
                .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Получение табулатуры по id: несуществующий id")
    void getTabById_fail_notFound() throws Exception {
        mockMvc.perform(get("/tabs/999")
            .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Генерация табулатуры: успех")
    void generateTab_success() throws Exception {
        GenerateTabRequest request = new GenerateTabRequest();
        request.setGenreId(1L);
        request.setSignature("4/4");
		request.setMusicKey(0);
		request.setBpm(140);
		request.setChordProgression("C-1,G-1,F-1,Am-1");
        request.setIp("127.0.0.1");

        MlServerGenerateResponse mlResp = new MlServerGenerateResponse();
        mlResp.setTabData("tab-data-sample");
        mlResp.setAudioData("audio-b64");
        when(mlClient.generateTab(any())).thenReturn(mlResp);
        when(generationLockService.tryLock(anyString())).thenReturn(true);

        mockMvc.perform(post("/tabs/generate")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
                .andExpect(jsonPath("$.signature").value("4/4"));
    }

    @Test
    @DisplayName("Генерация табулатуры: невалидное поле")
    void generateTab_fail_validationFail() throws Exception {
        GenerateTabRequest request = new GenerateTabRequest();
		request.setGenreId(1L);
		request.setSignature("4/4");
		request.setMusicKey(-5);
		request.setBpm(140);
		request.setChordProgression("C-1,G-1,F-1,Am-1");
		request.setIp("127.0.0.1");

        mockMvc.perform(post("/tabs/generate")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("musicKey: must be greater than or equal to 0"));
    }

    @Test
    @DisplayName("Генерация табулатуры: попытка отправить 2 запроса на генерацию")
    void generateTab_fail_tooManyRequests() throws Exception {
        GenerateTabRequest request = new GenerateTabRequest();
		request.setGenreId(1L);
		request.setSignature("4/4");
		request.setMusicKey(0);
		request.setBpm(140);
		request.setChordProgression("C-1,G-1,F-1,Am-1");
		request.setIp("127.0.0.1");

        when(generationLockService.tryLock(anyString())).thenReturn(false);

        mockMvc.perform(post("/tabs/generate")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("Сохранение табулатуры: успех")
    void saveTab_success() throws Exception {
        SaveTabRequest request = new SaveTabRequest();
        request.setTitle("Сохраняемая таба");
		request.setGenreId(1L);
		request.setSignature("4/4");
		request.setMusicKey(0);
		request.setBpm(140);
		request.setChordProgression("C-1,G-1,F-1,Am-1");
		request.setTabData("Табулатура");
		request.setAudioData("base-64");

        mockMvc.perform(post("/tabs")
            .header("Authorization", "Bearer " + TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        var tabs = tabRepository.findByUser(currentUser);
        var created = tabs.stream()
            .filter(t -> "Сохраняемая таба".equals(t.getTitle()))
            .findFirst();

        if (created.isPresent()) {
            Path uploadDir = Paths.get("src/test/resources/test-tabs");
            Path filePath = uploadDir.resolve(created.get().getAudioUrl());
            Files.deleteIfExists(filePath);
            tabRepository.delete(created.get());
        }
    }

    @Test
    @DisplayName("Сохранение табулатуры: пользователь не авторизован")
    void saveTab_fail_unauthorized() throws Exception {
        SaveTabRequest request = new SaveTabRequest();
		request.setTitle("Сохраняемая таба");
		request.setGenreId(1L);
		request.setSignature("4/4");
		request.setMusicKey(0);
		request.setBpm(140);
		request.setChordProgression("C-1,G-1,F-1,Am-1");
		request.setTabData("Табулатура");
		request.setAudioData("base-64");

        mockMvc.perform(post("/tabs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Сохранение табулатуры: невалидное поле tab_data")
    void saveTab_fail_validationFail() throws Exception {
        SaveTabRequest request = new SaveTabRequest();
		request.setTitle("Сохраняемая таба");
		request.setGenreId(1L);
		request.setSignature("4/4");
		request.setMusicKey(0);
		request.setBpm(140);
		request.setChordProgression("C-1,G-1,F-1,Am-1");
		request.setTabData("");
		request.setAudioData("base-64");

        mockMvc.perform(post("/tabs")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("tabData: Табулатура обязательна"));
    }

    @Test
    @DisplayName("Удаление табулатуры: успех")
    void deleteTab_success() throws Exception {
        Path uploadDir = Paths.get("src/test/resources/test-tabs");
        try {
            Files.createDirectories(uploadDir);
        } catch (Exception ignored) {}

        String filename = UUID.randomUUID() + ".b64";
        Path filePath = uploadDir.resolve(filename);
        Files.writeString(filePath, "test-audio-data");

        Tab tab = Tab.builder()
            .user(currentUser)
            .genre(genreRepository.findById(1L).orElseThrow())
            .title("to-delete")
            .chordProgression("C-1")
            .musicKey(0)
            .signature("4/4")
            .bpm(100)
            .tabData("x")
            .audioUrl(filename)
            .createdAt(OffsetDateTime.now())
            .build();

        tab = tabRepository.save(tab);

        mockMvc.perform(delete("/tabs/" + tab.getId())
            .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isNoContent());
        Files.deleteIfExists(filePath);
    }

    @Test
    @DisplayName("Удаление табулатуры: несуществующий id")
    void deleteTab_fail_notFound() throws Exception {
        mockMvc.perform(delete("/tabs/999")
            .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Удаление табулатуры: доступ запрещен")
    void deleteTab_fail_forbidden() throws Exception {

        mockMvc.perform(delete("/tabs/4")
                .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Удаление табулатуры: пользователь не авторизован")
    void deleteTab_fail_unauthorized() throws Exception {

        mockMvc.perform(delete("/tabs/11"))
            .andExpect(status().isUnauthorized());
    }
}