package com.alexey.tabgenerator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для запроса сохранения табулатуры пользователя в базе.
 */
@Getter
@Setter
public class SaveTabRequest {

    @NotBlank(message = "Название табулатуры обязательно")
    private String title;

    @NotNull(message = "ID жанра обязателен")
    private Long genreId;

    @NotBlank(message = "Музыкальный размер обязателен")
    private String signature;

    @NotNull(message = "Тональность обязательна")
    private Integer musicKey;

    @NotNull(message = "BPM обязателен")
    private Integer bpm;

    @NotBlank(message = "Последовательность аккордов обязательна")
    private String chordProgression;

    @NotBlank(message =  "Табулатура обязательна")
    private String tabData;

    @NotBlank(message = "Аудио-данные обязательны")
    private String audioData;
}