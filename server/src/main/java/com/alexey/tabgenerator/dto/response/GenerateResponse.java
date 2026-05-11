package com.alexey.tabgenerator.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO для ответа с информацией о сгенерированной табулатуре.
 */
@Getter
@Builder
public class GenerateResponse {

    private Long genreId;

    private String signature;

    private Integer musicKey;

    private Integer bpm;

    private String chordProgression;

    private String tabData;

    private String audioData;
}
