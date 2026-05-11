package com.alexey.tabgenerator.repository;

import com.alexey.tabgenerator.entity.PasswordResetToken;

import com.alexey.tabgenerator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Репозиторий для работы с токенами восстановления пароля.
 * Обеспечивает стандартные CRUD-операции и удаление по пользоватлям и истёкшей дате действия.
 */
@Repository
public interface PasswordResetTokenRepository
    extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByExpiresAtBefore(OffsetDateTime now);

    void deleteByUser(User user);
}
