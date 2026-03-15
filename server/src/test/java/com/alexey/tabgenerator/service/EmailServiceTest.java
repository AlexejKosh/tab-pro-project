package com.alexey.tabgenerator.service;

import com.alexey.tabgenerator.exception.EmailSendException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для {@link EmailService}
 *
 * Проверяют:
 * - Восстановление пароля
 *
 * Используют моки: {@link JavaMailSender}.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() throws Exception {
        Field fromEmailField = EmailService.class.getDeclaredField("fromEmail");
        fromEmailField.setAccessible(true);
        fromEmailField.set(emailService, "noreply@example.com");
    }

    @Test
    @DisplayName("Отправка сообщения: успех")
    void sendPasswordRecovery_success() {
        String email = "user@example.com";
        String newPassword = "abc12345";

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendPasswordRecovery(email, newPassword);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals("noreply@example.com", sentMessage.getFrom());
        assertEquals(email, sentMessage.getTo()[0]);
        assertEquals("Восстановление пароля на TabGen", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(newPassword));
    }

    @Test
    @DisplayName("Отправка сообщения: ошибка SMTP сервера")
    void sendPasswordRecovery_fail_mailSenderThrowsException() {
        String email = "user@example.com";
        String newPassword = "abc12345";

        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        EmailSendException ex = assertThrows(EmailSendException.class, () ->
            emailService.sendPasswordRecovery(email, newPassword)
        );

        assertTrue(ex.getMessage().contains("Не удалось отправить письмо на " + email));
        assertTrue(ex.getMessage().contains("SMTP error"));
    }
}