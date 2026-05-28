package com.alexey.tabgenerator.integration;

import com.alexey.tabgenerator.config.MlProperties;
import com.alexey.tabgenerator.dto.request.GenerateTabRequest;
import com.alexey.tabgenerator.dto.response.MlServerGenerateResponse;
import com.alexey.tabgenerator.exception.MlServerException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для {@link MlClient}.
 *
 * Проверяют:
 * - успешную генерацию табулатуры
 * - обработку различных ошибок при взаимодействии с ML сервером
 */
@ExtendWith(MockitoExtension.class)
class MlClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MlProperties mlProperties;

    @InjectMocks
    private MlClient mlClient;

    @BeforeEach
    void setUp() {
        when(mlProperties.getBaseUrl()).thenReturn("http://mlserver.ru");
        when(mlProperties.getGenerateEndpoint()).thenReturn("/generate");
    }

    @Test
    @DisplayName("Генерация табулатуры: успех")
    void generateTab_success() {
        GenerateTabRequest request = new GenerateTabRequest();
        request.setGenreId(1L);
        request.setSignature("4/4");
        request.setMusicKey(0);
        request.setBpm(120);
        request.setChordProgression("C-1,G-1");
        request.setIp("127.0.0.1");

        MlServerGenerateResponse respBody = new MlServerGenerateResponse();
        respBody.setTabData("tab-data");
        respBody.setAudioData("audio-b64");

        ResponseEntity<MlServerGenerateResponse> responseEntity = new ResponseEntity<>(respBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(MlServerGenerateResponse.class)))
            .thenReturn(responseEntity);

        MlServerGenerateResponse result = mlClient.generateTab(request);

        assertNotNull(result);
        assertEquals("tab-data", result.getTabData());
        assertEquals("audio-b64", result.getAudioData());

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq("http://mlserver.ru/generate"), captor.capture(), eq(MlServerGenerateResponse.class));

        Object body = captor.getValue().getBody();
        assertTrue(body instanceof GenerateTabRequest);
        GenerateTabRequest sent = (GenerateTabRequest) body;
        assertEquals(request.getGenreId(), sent.getGenreId());
        assertEquals(request.getSignature(), sent.getSignature());
        assertEquals(request.getMusicKey(), sent.getMusicKey());
        assertEquals(request.getBpm(), sent.getBpm());
        assertEquals(request.getChordProgression(), sent.getChordProgression());
        assertEquals(request.getIp(), sent.getIp());
    }

    @Test
    @DisplayName("Генерация табулатуры: пустой ответ от ML")
    void generateTab_fail_emptyBody() {
        GenerateTabRequest request = new GenerateTabRequest();
        request.setGenreId(1L);

        ResponseEntity<MlServerGenerateResponse> responseEntity = new ResponseEntity<MlServerGenerateResponse>
            ((MlServerGenerateResponse) null, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(MlServerGenerateResponse.class)))
            .thenReturn(responseEntity);

        MlServerException ex = assertThrows(MlServerException.class, () -> mlClient.generateTab(request));
        assertTrue(ex.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR);
        assertTrue(ex.getMessage().contains("Внутренняя ошибка"));
    }

    @Test
    @DisplayName("Генерация табулатуры: HTTP ошибка от ML сервера")
    void generateTab_fail_httpStatusException() {
        GenerateTabRequest request = new GenerateTabRequest();
        request.setGenreId(1L);

        HttpClientErrorException httpEx = HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY, "error-body".getBytes(), null);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(MlServerGenerateResponse.class)))
            .thenThrow(httpEx);

        MlServerException ex = assertThrows(MlServerException.class, () -> mlClient.generateTab(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("error-body"));
    }

    @Test
    @DisplayName("Генерация табулатуры: ML сервер недоступен")
    void generateTab_fail_resourceAccess() {
        GenerateTabRequest request = new GenerateTabRequest();
        request.setGenreId(1L);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(MlServerGenerateResponse.class)))
            .thenThrow(new ResourceAccessException("I/O error"));

        MlServerException ex = assertThrows(MlServerException.class, () -> mlClient.generateTab(request));
        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatus());
        assertTrue(ex.getMessage().contains("ML сервер недоступен"));
    }

    @Test
    @DisplayName("Генерация табулатуры: неизвестная ошибка")
    void generateTab_fail_genericException() {
        GenerateTabRequest request = new GenerateTabRequest();
        request.setGenreId(1L);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(MlServerGenerateResponse.class)))
            .thenThrow(new RuntimeException("boom"));

        MlServerException ex = assertThrows(MlServerException.class, () -> mlClient.generateTab(request));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
        assertTrue(ex.getMessage().contains("Внутренняя ошибка"));
    }
}
