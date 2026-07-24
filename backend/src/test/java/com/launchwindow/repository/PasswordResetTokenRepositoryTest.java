package com.launchwindow.repository;

import com.launchwindow.model.AppUser;
import com.launchwindow.model.PasswordResetToken;
import com.launchwindow.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class PasswordResetTokenRepositoryTest {
    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private AppUserRepository userRepository;

    @Test
    void findByTokenHash_returnsStoredToken() {
        AppUser user = saveUser();

        PasswordResetToken token = tokenRepository.save(
                        new PasswordResetToken(user, "stored-token-hash", Instant.parse("2026-07-24T12:30:00Z")));

        var result = tokenRepository.findByTokenHash("stored-token-hash");

        assertThat(result).contains(token);
    }

    @Test
    void latestTokenForUserIsReturnedFirst() {
        AppUser user = saveUser();

        tokenRepository.saveAndFlush(
                new PasswordResetToken(user, "first-token-hash", Instant.parse("2026-07-24T12:30:00Z")));

        PasswordResetToken second = tokenRepository.saveAndFlush(
                        new PasswordResetToken(user, "second-token-hash", Instant.parse("2026-07-24T13:00:00Z")));

        var result = tokenRepository.findFirstByUser_IdOrderByCreatedAtDescIdDesc(user.getId());

        assertThat(result).contains(second);
    }

    @Test
    void deleteAllByUserId_removesUsersTokens() {
        AppUser user = saveUser();

        tokenRepository.save(
                new PasswordResetToken(user, "token-to-delete", Instant.parse("2026-07-24T12:30:00Z")));

        tokenRepository.deleteAllByUser_Id(user.getId());
        tokenRepository.flush();

        assertThat(tokenRepository.findByTokenHash("token-to-delete")).isEmpty();
    }

    private AppUser saveUser() {
        return userRepository.save(
                new AppUser("spacefan", "space@example.com", "password-hash", Role.USER));
    }
}