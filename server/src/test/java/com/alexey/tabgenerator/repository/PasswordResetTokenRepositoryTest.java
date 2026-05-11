package com.alexey.tabgenerator.repository;

import com.alexey.tabgenerator.entity.PasswordResetToken;
import com.alexey.tabgenerator.entity.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Поиск токена по значению: успех")
    void findByToken_success() {
        User user = userRepository.findById(1L).orElseThrow();

        PasswordResetToken token = PasswordResetToken.builder()
            .user(user)
            .token("unique-token-123")
            .expiresAt(OffsetDateTime.now().plusHours(1))
            .build();

        passwordResetTokenRepository.save(token);

        Optional<PasswordResetToken> fromDb = passwordResetTokenRepository.findByToken("unique-token-123");

        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Поиск токена по значению: не найден")
    void findByToken_fail_notFound() {
        Optional<PasswordResetToken> fromDb = passwordResetTokenRepository.findByToken("no-such-token-xyz");

        assertThat(fromDb).isEmpty();
    }

    @Test
    @DisplayName("Удаление просроченных токенов: успех")
    void deleteByExpiresAtBefore_success() {
        User user = userRepository.findById(1L).orElseThrow();

        PasswordResetToken expired = PasswordResetToken.builder()
            .user(user)
            .token("expired-token")
            .expiresAt(OffsetDateTime.now().minusHours(1))
            .build();

        PasswordResetToken valid = PasswordResetToken.builder()
            .user(user)
            .token("valid-token")
            .expiresAt(OffsetDateTime.now().plusHours(2))
            .build();

        passwordResetTokenRepository.save(expired);
        passwordResetTokenRepository.save(valid);

        passwordResetTokenRepository.deleteByExpiresAtBefore(OffsetDateTime.now());

        List<PasswordResetToken> all = passwordResetTokenRepository.findAll();

        assertThat(all).extracting(PasswordResetToken::getToken).doesNotContain("expired-token");
        assertThat(all).extracting(PasswordResetToken::getToken).contains("valid-token");
    }

    @Test
    @DisplayName("Удаление токенов пользователя: успех")
    void deleteByUser_success() {
        User user1 = userRepository.findById(1L).orElseThrow();
        User user2 = userRepository.findById(2L).orElseThrow();

        PasswordResetToken t1 = PasswordResetToken.builder()
            .user(user1)
            .token("user1-token")
            .expiresAt(OffsetDateTime.now().plusHours(1))
            .build();

        PasswordResetToken t2 = PasswordResetToken.builder()
            .user(user2)
            .token("user2-token")
            .expiresAt(OffsetDateTime.now().plusHours(1))
            .build();

        passwordResetTokenRepository.save(t1);
        passwordResetTokenRepository.save(t2);

        passwordResetTokenRepository.deleteByUser(user1);

        List<PasswordResetToken> remaining = passwordResetTokenRepository.findAll();

        assertThat(remaining).extracting(PasswordResetToken::getToken).doesNotContain("user1-token");
        assertThat(remaining).extracting(PasswordResetToken::getToken).contains("user2-token");
    }
}
