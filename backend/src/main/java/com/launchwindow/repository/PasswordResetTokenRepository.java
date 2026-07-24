package com.launchwindow.repository;

import com.launchwindow.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    Optional<PasswordResetToken> findFirstByUser_IdOrderByCreatedAtDescIdDesc(Long userId);

    void deleteAllByUser_Id(Long userId);
}