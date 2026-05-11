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
    public void sendPasswordRecovery(String email, String token) {
        log.debug(
            "Попытка отправки письма для восстановления пароля: email={}", email
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Восстановление пароля на TabGen");
        message.setText(
            "Ваша ссылка на восстановление пароля: \n" +
                "http://localhost:8081/auth/recover-password/" + token +
                "\n\nЖелаем всего наилучшего!"
        );

        try {
            mailSender.send(message);

            log.info(
                "Письмо для восстановления пароля успешно отправлено: email={}", email
            );
        } catch (Exception e) {
            throw new EmailSendException(
                "Не удалось отправить письмо на " + email + ", причина: " + e.getMessage()
            );
        }
    }
}