package com.alexey.tabgenerator.service;

import com.alexey.tabgenerator.dto.request.ChangePasswordRequest;
import com.alexey.tabgenerator.dto.response.UserResponse;
import com.alexey.tabgenerator.entity.User;
import com.alexey.tabgenerator.entity.Tab;
import com.alexey.tabgenerator.exception.UnauthorizedException;
import com.alexey.tabgenerator.repository.UserRepository;
import com.alexey.tabgenerator.repository.TabRepository;
import com.alexey.tabgenerator.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для работы с пользователями.
 * Предоставляет методы получения текущего пользователя, смены пароля и удаления аккаунта.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TabRepository tabRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final TabService tabService;

    /**
     * Получение информации о текущем авторизованном пользователе.
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {

        User user = securityUtils.getCurrentUser();

        log.debug(
            "Полученена информация о текущем пользователе: username={}",
            user.getUsername()
        );

        return UserResponse.fromEntity(user);
    }

    /**
     * Смена пароля текущего пользователя.
     * Проверяет корректность старого пароля перед установкой нового.
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        User user = securityUtils.getCurrentUser();

        log.debug(
            "Попытка смены пароля: username={}",
            user.getUsername()
        );

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Старый пароль указан неверно");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info(
            "Пароль успешно изменён: username={}",
            user.getUsername()
        );
    }

    /**
     * Удаление текущего пользователя из системы.
     */
    @Transactional
    public void deleteCurrentUser() {

        User user = securityUtils.getCurrentUser();

        log.debug(
            "Попытка удаления пользователя: username={}",
            user.getUsername()
        );

        List<Tab> tabs = tabRepository.findByUser(user);

        tabs.stream()
            .map(Tab::getId)
            .forEach(tabService::deleteTab);

        userRepository.delete(user);

        log.warn(
            "Пользователь удалён: username={}",
            user.getUsername()
        );
    }
}