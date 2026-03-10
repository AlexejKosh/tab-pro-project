package com.alexey.tabgenerator.service;

import com.alexey.tabgenerator.dto.request.ChangePasswordRequest;
import com.alexey.tabgenerator.dto.response.UserResponse;
import com.alexey.tabgenerator.entity.User;
import com.alexey.tabgenerator.exception.UnauthorizedException;
import com.alexey.tabgenerator.repository.UserRepository;
import com.alexey.tabgenerator.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для работы с пользователями.
 * Предоставляет методы получения текущего пользователя, смены пароля и удаления аккаунта.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    /**
     * Получение информации о текущем авторизованном пользователе.
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {

        log.debug("Получение информации о текущем пользователе");

        User user = securityUtils.getCurrentUser();

        log.info("Информация о текущем пользователе успешно получена: username={}", user.getUsername());

        return UserResponse.fromEntity(user);
    }

    /**
     * Смена пароля текущего пользователя.
     * Проверяет корректность старого пароля перед установкой нового.
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        User user = securityUtils.getCurrentUser();

        log.debug("Попытка смены пароля для пользователя: username={}", user.getUsername());

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Старый пароль указан неверно");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Пароль успешно изменён для пользователя: username={}", user.getUsername());
    }

    /**
     * Удаление текущего пользователя из системы.
     */
    @Transactional
    public void deleteCurrentUser() {

        log.debug("Удаление текущего пользователя");

        User user = securityUtils.getCurrentUser();

        userRepository.delete(user);

        log.info("Пользователь успешно удалён: username={}", user.getUsername());
    }
}