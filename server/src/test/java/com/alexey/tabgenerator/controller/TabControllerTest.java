package com.alexey.tabgenerator.controller;

import com.alexey.tabgenerator.dto.request.GenerateTabRequest;
import com.alexey.tabgenerator.dto.request.SaveTabRequest;
import com.alexey.tabgenerator.entity.*;
import com.alexey.tabgenerator.integration.MlClient;
import com.alexey.tabgenerator.repository.TabRepository;
import com.alexey.tabgenerator.security.JwtService;
import com.alexey.tabgenerator.security.SecurityUtils;
import com.alexey.tabgenerator.security.UserDetailsServiceImpl;
import com.alexey.tabgenerator.service.GenerationLockService;

import jakarta.transaction.Transactional;
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

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
 * Используют моки: {@link TabRepository}, {@link MlClient},
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

    // Мок репозитория для сущности Tab, поскольку
    // H2 некорректно работает с JSONB, из-за чего
    // доступ к БД изолируется
    @MockitoBean
    private TabRepository tabRepository;

    // Мок ML клиента, чтобы не выполнять реальные
    // HTTP-запросы к ML серверу
    @MockitoBean
    private MlClient mlClient;

    // Мок сервиса блокировки частых запросов с одного IP
    @MockitoBean
    private GenerationLockService generationLockService;

    // Мок компонентов безопасности
    // для  иммитации авторизованного пользователя
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
        currentUser = new com.alexey.tabgenerator.entity.User();
        currentUser.setId(7L);
        currentUser.setUsername("alexeyKo");
        currentUser.setEmail("miner_847@mail.ru");

        UserDetails userDetails = User
            .withUsername("alexeyKo")
            .password("encoded")
            .authorities("ROLE_USER")
            .build();

        when(jwtService.extractUsername(TOKEN)).thenReturn("alexeyKo");
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(true);
        when(jwtService.buildAuthentication(userDetails)).thenCallRealMethod();
        when(userDetailsService.loadUserByUsername("alexeyKo")).thenReturn(userDetails);
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    @DisplayName("Получение табулатур: успех")
    void getAllTabs_success() throws Exception {
        Tab tab = new Tab();
        tab.setId(11L);
        tab.setTitle("Типо Джеймс Браун");
        tab.setUser(currentUser);
        tab.setGenre(new Genre(1L, "Rock"));
        tab.setSignature("4/4");
        tab.setChordProgression(List.of(List.of("C", 1)));
        tab.setTabData(List.of(List.of(1,2,3,4)));
        tab.setCreatedAt(OffsetDateTime.now());

        when(tabRepository.findByUser(any())).thenReturn(List.of(tab));

        mockMvc.perform(get("/tabs")
                .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(11))
            .andExpect(jsonPath("$[0].title").value("Типо Джеймс Браун"));
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
        Tab tab = new Tab();
        tab.setId(11L);
        tab.setTitle("Типо Джеймс Браун");
        tab.setUser(currentUser);
        tab.setGenre(new Genre(1L, "Rock"));
        tab.setSignature("4/4");
        tab.setChordProgression(List.of(List.of("C", 1)));
        tab.setTabData(List.of(List.of(1,2,3,4)));
        tab.setCreatedAt(OffsetDateTime.now());

        when(tabRepository.findById(11L)).thenReturn(Optional.of(tab));

        mockMvc.perform(get("/tabs/11")
                .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(11))
            .andExpect(jsonPath("$.title").value("Типо Джеймс Браун"));
    }

    @Test
    @DisplayName("Получение табулатуры по id: доступ запрещён")
    void getTabById_fail_forbidden() throws Exception {
        Tab tab = new Tab();
        tab.setId(9L);
        tab.setTitle("Чужой таб");
        tab.setUser(com.alexey.tabgenerator.entity.User.builder()
            .id(8L)
            .username("other")
            .email("other@mail.ru")
            .build());
        tab.setGenre(new Genre(1L, "Rock"));

        when(tabRepository.findById(9L)).thenReturn(Optional.of(tab));

        mockMvc.perform(get("/tabs/9")
                .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Получение табулатуры по id: несуществующий id")
    void getTabById_fail_notFound() throws Exception {
        when(tabRepository.findById(20L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/tabs/20")
                .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Генерация табулатуры: успех")
    void generateTab_success() throws Exception {
        GenerateTabRequest request = new GenerateTabRequest();
        request.setTitle("Новая таба");
        request.setGenreId(1L);
        request.setSignature("4/4");
        request.setChordProgression(List.of(List.of("C", 1)));

        List<List<Integer>> tabData = List.of(List.of(1,2,3,4));
        when(mlClient.generateTab(anyString(), any(), anyString(), anyLong())).thenReturn(tabData);
        when(generationLockService.tryLock(anyString())).thenReturn(true);

        mockMvc.perform(post("/tabs/generate")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Новая таба"));
    }

    @Test
    @DisplayName("Генерация табулатуры: невалидное поле")
    void generateTab_fail_validationFail() throws Exception {
        GenerateTabRequest request = new GenerateTabRequest();
        request.setTitle("");
        request.setGenreId(1L);
        request.setSignature("4/4");
        request.setChordProgression(List.of(List.of("C", 1)));

        mockMvc.perform(post("/tabs/generate")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("title: Название табулатуры обязательно"));
    }

    @Test
    @DisplayName("Генерация табулатуры: попытка отправить 2 запроса на генерацию")
    void generateTab_fail_tooManyRequests() throws Exception {
        GenerateTabRequest request = new GenerateTabRequest();
        request.setTitle("Новая таба");
        request.setGenreId(1L);
        request.setSignature("4/4");
        request.setChordProgression(List.of(List.of("C", 1)));

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
        request.setChordProgression(List.of(List.of("C", 1)));
        request.setTabData(List.of(List.of(1,2,3,4)));

        when(tabRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tabRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(post("/tabs")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Сохранение табулатуры: пользователь не авторизован")
    void saveTab_fail_unauthorized() throws Exception {
        SaveTabRequest request = new SaveTabRequest();
        request.setTitle("Сохраняемая таба");
        request.setGenreId(1L);
        request.setSignature("4/4");
        request.setChordProgression(List.of(List.of("C", 1)));
        request.setTabData(List.of(List.of(1,2,3,4)));

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
        request.setChordProgression(List.of(List.of("C", 1)));
        request.setTabData(Collections.emptyList());

        mockMvc.perform(post("/tabs")
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("tabData: Данные табулатуры обязательны"));
    }

    @Test
    @DisplayName("Удаление табулатуры: успех")
    void deleteTab_success() throws Exception {
        Tab tab = new Tab();
        tab.setId(11L);
        tab.setUser(currentUser);

        when(tabRepository.findById(11L)).thenReturn(Optional.of(tab));

        mockMvc.perform(delete("/tabs/11")
                .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Удаление табулатуры: несуществующий id")
    void deleteTab_fail_notFound() throws Exception {
        when(tabRepository.findById(20L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/tabs/20")
                .header("Authorization", "Bearer " + TOKEN))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Удаление табулатуры: доступ запрещен")
    void deleteTab_fail_forbidden() throws Exception {
        Tab tab = new Tab();
        tab.setId(9L);
        tab.setUser(com.alexey.tabgenerator.entity.User.builder()
            .id(8L)
            .username("other")
            .email("other@mail.ru")
            .build()
        );

        when(tabRepository.findById(9L)).thenReturn(Optional.of(tab));

        mockMvc.perform(delete("/tabs/9")
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