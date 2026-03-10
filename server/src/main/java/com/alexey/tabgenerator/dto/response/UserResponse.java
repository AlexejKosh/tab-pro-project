package com.alexey.tabgenerator.dto.response;

import com.alexey.tabgenerator.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * DTO для ответа с информацией о пользователе.
 */
@Getter
@Builder
public class UserResponse {

    private Long id;

    private String username;

    private String email;

    private OffsetDateTime createdAt;

    /**
     * Создание DTO из сущности User.
     */
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
