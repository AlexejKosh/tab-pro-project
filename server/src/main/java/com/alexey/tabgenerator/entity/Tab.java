package com.alexey.tabgenerator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Сущность табулатуры.
 * Хранит информацию о конкретной табулатуре пользователя: аккорды,
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

    @Column(length = 5, nullable = false)
    @NotBlank(message = "Размер обязателен")
    private String signature;

    @Column(name = "chord_progression", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<List<Object>> chordProgression;

    @Column(name = "tab_data", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<List<Integer>> tabData;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}