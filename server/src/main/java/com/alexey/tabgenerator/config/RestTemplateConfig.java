package com.alexey.tabgenerator.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Конфигурация для создания RestTemplate как Spring Bean.
 * Позволяет внедрять RestTemplate через @Autowired / @RequiredArgsConstructor
 * во все сервисы, которым нужно делать HTTP запросы.
 */
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final MlProperties mlProperties;

    @Bean
    public RestTemplate restTemplate() {

        // Создаем фабрику HTTP-запросов для настройки таймаутов RestTemplate
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // Таймаут установки соединения с ML сервером (5 секунд)
        factory.setConnectTimeout(5000);

        // Таймаут ожидания ответа от ML сервера (берётся из application.yaml)
        factory.setReadTimeout(mlProperties.getTimeoutSeconds() * 1000);

        return new RestTemplate(factory);
    }
}