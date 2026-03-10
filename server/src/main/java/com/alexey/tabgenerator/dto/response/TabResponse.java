package com.alexey.tabgenerator.dto.response;

import com.alexey.tabgenerator.entity.Tab;

import com.alexey.tabgenerator.exception.JsonConversionException;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

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

    private List<List<Object>> chordProgression;

    private List<List<Integer>> tabData;

    private OffsetDateTime createdAt;

    /**
     * Создание DTO из сущности Tab.
     */
    public static TabResponse fromEntity(Tab tab) {
        try {
            return TabResponse.builder()
                .id(tab.getId())
                .genreId(tab.getGenre().getId())
                .title(tab.getTitle())
                .signature(tab.getSignature())
                .chordProgression(tab.getChordProgression())
                .tabData(tab.getTabData())
                .createdAt(tab.getCreatedAt())
                .build();
        } catch (Exception e) {
            throw new JsonConversionException("Ошибка преобразования JSON табулатуры, "+ e);
        }
    }
}