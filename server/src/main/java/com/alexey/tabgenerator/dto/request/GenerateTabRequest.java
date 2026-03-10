package com.alexey.tabgenerator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO для запроса генерации новой табулатуры через ML-сервер.
 */
@Getter
@Setter
public class GenerateTabRequest {

    @NotBlank(message = "Название табулатуры обязательно")
    private String title;

    @NotNull(message = "ID жанра обязателен")
    private Long genreId;

    @NotBlank(message = "Музыкальный размер обязателен")
    private String signature;

    @NotEmpty(message = "Последовательность аккордов обязательна")
    private List<List<Object>> chordProgression;
}