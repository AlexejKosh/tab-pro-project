package com.alexey.tabgenerator.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.alexey.tabgenerator.entity.Tab;

import java.time.OffsetDateTime;

/**
 * DTO для ответа с краткой информацией о табулатуре.
 */
@Getter
@Builder
public class TabSummaryResponse {
    private Long id;

    private String title;

    private String signature;

    private Long genreId;

    private String chordProgression;

    private OffsetDateTime createdAt;

    /**
     * Создание DTO из сущности Tab.
     */
    public static TabSummaryResponse fromEntity(Tab tab) {
        return TabSummaryResponse.builder()
            .id(tab.getId())
            .title(tab.getTitle())
            .signature(tab.getSignature())
            .genreId(tab.getGenre().getId())
            .chordProgression(tab.getChordProgression())
            .createdAt(tab.getCreatedAt())
            .build();
    }
}
