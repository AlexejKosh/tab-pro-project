package com.alexey.tabgenerator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для {@link GenerationLockService}.
 *
 * Проверяют:
 * - блокировки для IP
 * - снятие блокировки
 */
class GenerationLockServiceTest {

    private GenerationLockService lockService;

    @BeforeEach
    void setUp() {
        lockService = new GenerationLockService();
    }

    @Test
    @DisplayName("Блокировка для id: блокировка успешно установлена")
    void tryLock_returnsTrueIfNotLocked() {
        String ip = "192.168.0.1";
        boolean result = lockService.tryLock(ip);

        assertTrue(result, "Ожидаем, что первый вызов tryLock вернёт true");
    }

    @Test
    @DisplayName("Блокировка для ip: блокировка уже существует")
    void tryLock_returnsFalseIfAlreadyLocked() {
        String ip = "192.168.0.2";
        lockService.tryLock(ip);
        boolean result = lockService.tryLock(ip);

        assertFalse(result, "Ожидаем, что второй вызов tryLock для того же IP вернёт false");
    }

    @Test
    @DisplayName("Снятие блокировки ip: блокировка успешно снята")
    void unlock_RemovesIpFromActiveSet() {
        String ip = "192.168.0.3";
        lockService.tryLock(ip);
        lockService.unlock(ip);
        boolean result = lockService.tryLock(ip);

        assertTrue(result, "После unlock IP можно снова заблокировать, ожидаем true");
    }

    @Test
    @DisplayName("Снятие блокировки ip: ip отсутствует в активных блокировках")
    void unlock_DoesNothingIfIpNotPresent() {
        String ip = "192.168.0.4";
        lockService.unlock(ip);

        assertTrue(lockService.tryLock(ip));
    }
}