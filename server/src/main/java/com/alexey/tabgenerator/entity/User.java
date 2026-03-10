package com.alexey.tabgenerator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Сущность пользователя.
 * Хранит основную информацию о пользователе: имя, email, хэш пароля и дату создания.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 25, nullable = false, unique = true)
    @NotBlank(message = "Имя пользователя обязательно")
    private String username;

    @Column(length = 255, nullable = false, unique = true)
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email")
    private String email;

    @Column(name = "password_hash", nullable = false)
    @NotBlank(message = "Пароль обязателен")
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}