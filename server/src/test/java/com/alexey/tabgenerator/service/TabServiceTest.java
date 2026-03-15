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
import com.alexey.tabgenerator.integration.MlClient;
import com.alexey.tabgenerator.repository.GenreRepository;
import com.alexey.tabgenerator.repository.TabRepository;
import com.alexey.tabgenerator.security.SecurityUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты для {@link TabService}.
 *
 * Проверяют:
 * - получение табулатур
 * - генерацию табулатуры
 * - сохранение табулатуры
 * - удаление табулатуры
 *
 * Используют моки: {@link TabRepository},
 * {@link GenreRepository}, {@link MlClient},
 * {@link SecurityUtils}.
 */
@ExtendWith(MockitoExtension.class)
class TabServiceTest {

    // Мок репозитория для сущности Tab, поскольку
    // H2 некорректно работает с JSONB, из-за чего
    // доступ к БД изолируется
    @Mock
    private TabRepository tabRepository;

    // Мок репозитория для сущности Genre,
    // для имитации обращения к БД
    @Mock
    private GenreRepository genreRepository;

    // Мок ML клиента, чтобы не выполнять реальные
    // HTTP-запросы к ML серверу
    @Mock
    private MlClient mlClient;

    // Мок компонентов безопасности
    // для иммитации авторизованного пользователя
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private TabService tabService;

    private User mockUser;
    private Genre mockGenre;
    private Tab mockTab;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
            .id(1L)
            .username("testuser")
            .build();

        mockGenre = Genre.builder()
            .id(10L)
            .name("Rock")
            .build();

        mockTab = Tab.builder()
            .id(100L)
            .user(mockUser)
            .genre(mockGenre)
            .title("Test Tab")
            .signature("4/4")
            .chordProgression(List.of(List.of("C", 3)))
            .tabData(List.of(List.of(0, 1, 2, 5)))
            .createdAt(OffsetDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Получение всех табулатур пользователя: успех")
    void testGetAllTabs_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(tabRepository.findByUser(mockUser)).thenReturn(List.of(mockTab));

        List<TabResponse> result = tabService.getAllTabs();

        assertEquals(1, result.size());
        assertEquals("Test Tab", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Получение табулатуры по id: успех")
    void testGetTabById_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(tabRepository.findById(100L)).thenReturn(Optional.of(mockTab));

        TabResponse response = tabService.getTabById(100L);

        assertEquals("Test Tab", response.getTitle());
    }

    @Test
    @DisplayName("Получение табулатуры по id: несуществующий id")
    void testGetTabById_fail_notFound() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(tabRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tabService.getTabById(999L));
    }

    @Test
    @DisplayName("Получение табулатуры по id: доступ запрещен")
    void testGetTabById_fail_forbidden() {
        User anotherUser = User.builder().id(2L).username("other").build();
        Tab tabOtherUser = Tab.builder().id(200L).user(anotherUser).build();

        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(tabRepository.findById(200L)).thenReturn(Optional.of(tabOtherUser));

        assertThrows(ForbiddenException.class, () -> tabService.getTabById(200L));
    }

    @Test
    @DisplayName("Генерация табулатуры: успех")
    void testGenerateTab_success() {
        GenerateTabRequest request = new GenerateTabRequest();
        request.setTitle("Generated Tab");
        request.setGenreId(10L);
        request.setSignature("4/4");
        request.setChordProgression(List.of(List.of("C", "G")));

        when(genreRepository.findById(10L)).thenReturn(Optional.of(mockGenre));
        when(mlClient.generateTab(anyString(), anyList(), anyString(), anyLong()))
            .thenReturn(List.of(List.of(0, 1, 2)));

        GenerateResponse response = tabService.generateTab(request);

        assertEquals("Generated Tab", response.getTitle());
        assertEquals(10L, response.getGenreId());
        assertNotNull(response.getTabData());
    }

    @Test
    @DisplayName("Сохранение табулатуры: успех")
    void testSaveTab_success() {
        SaveTabRequest request = new SaveTabRequest();
        request.setTitle("New Tab");
        request.setGenreId(10L);
        request.setSignature("4/4");
        request.setChordProgression(List.of(List.of("C", "G")));
        request.setTabData(List.of(List.of(0, 1, 2)));

        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(genreRepository.findById(10L)).thenReturn(Optional.of(mockGenre));
        when(tabRepository.save(any(Tab.class))).thenAnswer(invocation -> {
            Tab tab = invocation.getArgument(0);
            tab.setId(123L);
            return tab;
        });

        tabService.saveTab(request);

        verify(tabRepository).save(any(Tab.class));
    }

    @Test
    @DisplayName("Удаление табулатуры: успех")
    void testDeleteTab_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(tabRepository.findById(100L)).thenReturn(Optional.of(mockTab));

        tabService.deleteTab(100L);

        verify(tabRepository).delete(mockTab);
    }

    @Test
    @DisplayName("Удаление табулатуры: доступ запрещен")
    void testDeleteTab_fail_forbidden() {
        User anotherUser = User.builder().id(2L).username("other").build();
        Tab tabOtherUser = Tab.builder().id(200L).user(anotherUser).build();

        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(tabRepository.findById(200L)).thenReturn(Optional.of(tabOtherUser));

        assertThrows(ForbiddenException.class, () -> tabService.deleteTab(200L));
    }

    @Test
    @DisplayName("Удаление табулатуры: несуществующий id")
    void testDeleteTab_fail_notFound() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(tabRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tabService.deleteTab(999L));
    }
}
