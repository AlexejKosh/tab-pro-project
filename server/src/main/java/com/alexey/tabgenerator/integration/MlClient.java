package com.alexey.tabgenerator.integration;

import com.alexey.tabgenerator.config.MlProperties;
import com.alexey.tabgenerator.dto.request.GenerateTabRequest;
import com.alexey.tabgenerator.dto.response.MlServerGenerateResponse;
import com.alexey.tabgenerator.exception.MlServerException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Сервис для интеграции с ML сервером генерации табулатур.
 * Отвечает за формирование запроса, отправку на ML сервер
 * и получение результата в виде строки табулатуры tabData
 * и закодированную строку в Base64 - mp3 аудио.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MlClient {

    private final RestTemplate restTemplate;        // HTTP клиент для отправки запросов
    private final MlProperties mlProperties;        // Настройки ML сервера (URL и endpoint)

    /**
     * Генерация табулатуры через ML сервер.
     */
    public MlServerGenerateResponse generateTab(GenerateTabRequest request) {

        String url = mlProperties.getBaseUrl() + mlProperties.getGenerateEndpoint();

        log.debug("Отправка запроса на ML сервер: url={}, musicKey={}, genreId{}, bpm={}",
            url, request.getMusicKey(), request.getGenreId(), request.getBpm());

        try {
            HttpEntity<GenerateTabRequest> entity = new HttpEntity<>(request);

            ResponseEntity<MlServerGenerateResponse> responseEntity =
                restTemplate.postForEntity(
                    url,
                    entity,
                    MlServerGenerateResponse.class
                );

            MlServerGenerateResponse response = responseEntity.getBody();

            if (response == null) {
                throw new MlServerException(
                    "Пустой ответ от ML сервера",
                    HttpStatus.BAD_GATEWAY
                );
            }

            log.info("Успешно получен ответ от ML сервера: musicKey={}, genreId{}, bpm={}",
                request.getMusicKey(), request.getGenreId(), request.getBpm());

            return response;

        } catch (HttpStatusCodeException e) {
            // Ошибка, которую вернул FastAPI
            throw new MlServerException(
                e.getResponseBodyAsString(),
                HttpStatus.valueOf(e.getStatusCode().value())
            );

        } catch (ResourceAccessException e) {
            // ML сервер недоступен
            throw new MlServerException(
                "ML сервер недоступен",
                HttpStatus.BAD_GATEWAY
            );

        } catch (Exception e) {
            // Неожиданная ошибка backend
            throw new MlServerException(
                "Внутренняя ошибка при работе с ML сервером",
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}