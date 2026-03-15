package com.alexey.tabgenerator.integration;

import com.alexey.tabgenerator.config.MlProperties;
import com.alexey.tabgenerator.exception.MlServerException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Интеграционные тесты для {@link MlClient}.
 *
 * Проверяют:
 * - корректную генерацию табулатуры
 * - выброс исключений при ошибках сервера
 *
 * Используют моки: {@link RestTemplate}, {@link MlProperties}.
 */
@ExtendWith(MockitoExtension.class)
class MlClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MlProperties mlProperties;

    private ObjectMapper objectMapper;

    @InjectMocks
    private MlClient mlClient;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        when(mlProperties.getBaseUrl()).thenReturn("http://mlserver.com");
        when(mlProperties.getGenerateEndpoint()).thenReturn("/generate");

        mlClient = new MlClient(restTemplate, mlProperties, objectMapper);
    }

    @Test
    @DisplayName("Генерация табулатуры: успех")
    void generateTab_success() throws Exception {
        List<List<Object>> chordProgression = List.of(List.of(1, 2, 3), List.of(4, 5, 6));
        String title = "TestTab";
        String signature = "4/4";
        Long genreId = 1L;
        List<List<Integer>> mlResponse = List.of(List.of(0, 1, 2), List.of(3, 4, 5));
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(mlResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
            .thenReturn(responseEntity);

        List<List<Integer>> result = mlClient.generateTab(title, chordProgression, signature, genreId);
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq("http://mlserver.com/generate"), captor.capture(), eq(Object.class));
        Map<String,Object> requestBody = (Map<String, Object>) captor.getValue().getBody();

        assertEquals(mlResponse, result, "Метод должен корректно вернуть список табулатур");
        assertEquals(title, requestBody.get("title"));
        assertEquals(signature, requestBody.get("signature"));
        assertEquals(genreId, requestBody.get("genreId"));
        assertEquals(chordProgression, requestBody.get("chordProgression"));
    }

    @Test
    @DisplayName("Генерация табулатуры: ошибка ML сервера")
    void generateTab_fail_restError() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
            .thenThrow(new RuntimeException("Ошибка сервера"));

        assertThrows(MlServerException.class,
            () -> mlClient.generateTab("t", List.of(), "4/4", 1L),
            "Метод должен выбросить MlServerException при ошибке RestTemplate");
    }

    @Test
    @DisplayName("Генерация табулатуры: невалидные данные ML сервера")
    void generateTab_fail_invalidResponse() {
        Map<String,Object> badResponse = Map.of("invalid", "data");
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(badResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
            .thenReturn(responseEntity);

        assertThrows(MlServerException.class,
            () -> mlClient.generateTab("t", List.of(), "4/4", 1L),
            "Метод должен выбросить MlServerException при невозможности десериализации ответа");
    }
}