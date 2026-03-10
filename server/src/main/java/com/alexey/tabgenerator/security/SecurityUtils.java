package com.alexey.tabgenerator.security;

import com.alexey.tabgenerator.entity.User;
import com.alexey.tabgenerator.exception.UnauthorizedException;
import com.alexey.tabgenerator.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Утилитарный класс для работы с безопасностью.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Получение текущего авторизованного пользователя из контекста Spring Security.
     */
    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Пользователь не авторизован");
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
            .orElseThrow(() ->
                new UnauthorizedException("Пользователь не найден"));
    }
}