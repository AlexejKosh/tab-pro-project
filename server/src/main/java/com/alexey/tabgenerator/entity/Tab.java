package com.alexey.tabgenerator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Сущность табулатуры.
 * Хранит информацию о конкретной табулатуре пользователя: название, аккорды, BPM,
 * музыкальный размер, данные табулатуры, дату создания и связи с пользователем и жанром.
 */
@Entity
@Table(name = "tabs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "genre"})
public class Tab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    @Column(length = 50, nullable = false)
    @NotBlank(message = "Название обязательно")
    private String title;

    @Column(name = "chord_progression", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Аккордовая последовательность обязательна")
    private String chordProgression;

    @Column(name = "music_key", nullable = false)
    @Min(0)
    @Max(11)
    private Integer musicKey;

    @Column(length = 5, nullable = false)
    @NotBlank(message = "Размер обязателен")
    private String signature;

    @Column(nullable = false)
    @Min(50)
    @Max(200)
    private Integer bpm;

    @Column(name = "tab_data", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Табулатура не может быть пустой")
    private String tabData;

    @Column(name = "audio_url", nullable = false, length = 255)
    @NotBlank(message = "Ссылка на аудио обязательна")
    private String audioUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}