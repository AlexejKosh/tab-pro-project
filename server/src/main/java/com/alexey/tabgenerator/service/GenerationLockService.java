package com.alexey.tabgenerator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для ограничения параллельной генерации табулатур по IP-адресу.
 * Предоставляет методы блокировки и разблокировки IP пользователя
 * во время выполнения генерации.
 */
@Slf4j
@Service
public class GenerationLockService {

    private final Set<String> activeIps = ConcurrentHashMap.newKeySet();

    /**
     * Попытка установить блокировку для IP-адреса пользователя.
     * Возвращает true, если блокировка успешно установлена,
     * иначе false, если генерация для данного IP уже выполняется.
     */
    public boolean tryLock(String ip) {

        boolean locked = activeIps.add(ip);

        if (locked) {
            log.debug("Установлена блокировка генерации: ip={}", ip);
        } else {
            log.warn("Повторная попытка генерации при активной блокировке: ip={}", ip);
        }

        return locked;
    }

    /**
     * Снятие блокировки генерации для IP-адреса пользователя.
     */
    public void unlock(String ip) {

        activeIps.remove(ip);

        log.debug("Блокировка генерации снята: ip={}", ip);
    }
}
