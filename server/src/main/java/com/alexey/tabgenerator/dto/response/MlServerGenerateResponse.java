package com.alexey.tabgenerator.dto.response;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO для ответа ML-сервера с сгенерированной табулатурой и аудио
 */
@Getter
@Setter
public class MlServerGenerateResponse {

    private String tabData;

    private String audioData;
}
