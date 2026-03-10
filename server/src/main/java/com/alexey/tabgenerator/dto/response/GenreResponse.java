package com.alexey.tabgenerator.dto.response;

import com.alexey.tabgenerator.entity.Genre;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO для ответа с информацией о музыкальном жанре.
 */
@Getter
@Builder
public class GenreResponse {

    private Long id;

    private String name;

    public static GenreResponse fromEntity(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }
}
