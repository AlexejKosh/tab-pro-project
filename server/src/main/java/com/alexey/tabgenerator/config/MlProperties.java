package com.alexey.tabgenerator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация свойств для ML сервера.
 * Позволяет хранить базовый URL и endpoint генерации табулатур.
 */
@Configuration
@ConfigurationProperties(prefix = "ml")
@Getter
@Setter
public class MlProperties {

    private String baseUrl;
    private String generateEndpoint;
    private int timeoutSeconds;
}
