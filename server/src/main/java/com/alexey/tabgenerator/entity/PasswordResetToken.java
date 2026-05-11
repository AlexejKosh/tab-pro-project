package com.alexey.tabgenerator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Сущность токена для восстановления пароля.
 * Хранит уникальный токен, время его конца действия и связь с пользователем.
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 255, nullable = false, unique = true)
    @NotBlank(message = "Токен восстановления обязателен")
    private String token;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;
}
