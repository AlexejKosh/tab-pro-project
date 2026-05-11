package com.alexey.tabgenerator.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * DTO для ответа с информацией о табулатуре.
 */
@Getter
@Builder
public class TabResponse {

    private Long id;

    private Long genreId;

    private String title;

    private String signature;

    private Integer musicKey;

    private Integer bpm;

    private String chordProgression;

    private String tabData;

    private String audioData;

    private OffsetDateTime createdAt;
}