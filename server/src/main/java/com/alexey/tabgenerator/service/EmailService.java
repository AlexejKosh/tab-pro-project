package com.alexey.tabgenerator.service;

import com.alexey.tabgenerator.exception.EmailSendException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

/**
 * Сервис для отправки писем пользователям.
 * В данном классе реализована отправка писем для восстановления пароля.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Отправка письма с новым паролем пользователю.
     */
    public void sendPasswordRecovery(String email, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail); // теперь берём из конфигурации
        message.setTo(email);
        message.setSubject("Восстановление пароля на TabGen");
        message.setText("Ваш новый пароль для входа: " + newPassword + "\nРекомендуем сменить пароль после входа.");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailSendException(
                "Не удалось отправить письмо на " + email + ", причина: " + e.getMessage()
            );
        }
    }
}