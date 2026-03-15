package com.alexey.tabgenerator.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO для ответа с информацией о сгенерированной табулатуре.
 */
@Getter
@Builder
public class GenerateResponse {

    private Long genreId;

    private String title;

    private String signature;

    private List<List<Object>> chordProgression;

    private List<List<Integer>> tabData;
}
