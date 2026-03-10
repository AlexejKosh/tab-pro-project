package com.alexey.tabgenerator.integration;

import com.alexey.tabgenerator.config.MlProperties;
import com.alexey.tabgenerator.exception.MlServerException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для интеграции с ML сервером генерации табулатур.
 * Отвечает за формирование запроса, отправку на ML сервер
 * и получение результата в виде структуры List<List<Integer>>.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MlClient {

    private final RestTemplate restTemplate;        // HTTP клиент для отправки запросов
    private final MlProperties mlProperties;        // Настройки ML сервера (URL и endpoint)
    private final ObjectMapper objectMapper;        // Конвертер JSON

    /**
     * Генерация табулатуры через ML сервер.
     */
    public List<List<Integer>> generateTab(String title, List<List<Object>>chordProgression,
                                           String signature, Long genreId) {

        String url = mlProperties.getBaseUrl() + mlProperties.getGenerateEndpoint();
        Map<String, Object> request = new HashMap<>();

        request.put("title", title);
        request.put("chordProgression", chordProgression);
        request.put("signature", signature);
        request.put("genreId", genreId);

        log.debug("Отправка запроса на ML сервер: url={}", url);

        try {
            ResponseEntity<Object> responseEntity =
                restTemplate.postForEntity(url, new HttpEntity<>(request), Object.class);

            Object responseBody = responseEntity.getBody();

            log.info("Получен ответ от ML сервера");

            // Сериализация данных табулатуры, полученных в ответе с ML сервера
            String json = objectMapper.writeValueAsString(responseBody);

            return objectMapper.readValue(json, new TypeReference<List<List<Integer>>>(){});
        } catch (Exception e) {
            throw new MlServerException("Ошибка работы ML сервера, " + e);
        }
    }
}