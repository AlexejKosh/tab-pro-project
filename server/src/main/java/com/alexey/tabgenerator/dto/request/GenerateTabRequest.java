package com.alexey.tabgenerator.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для запроса генерации новой табулатуры через ML-сервер.
 */
@Getter
@Setter
public class GenerateTabRequest {

    @NotNull(message = "ID жанра обязателен")
    private Long genreId;

    @NotBlank(message = "Музыкальный размер обязателен")
    private String signature;

    @NotNull(message = "Тональность обязательна")
    @Min(0)
    @Max(11)
    private Integer musicKey;

    @NotNull(message = "BPM обязателен")
    @Min(50)
    @Max(200)
    private Integer bpm;

    @NotBlank(message = "Последовательность аккордов обязательна")
    private String chordProgression;

    private String ip;
}