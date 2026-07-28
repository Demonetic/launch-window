package com.launchwindow.service.auth;

import com.launchwindow.config.properties.PasswordResetProperties;
import com.launchwindow.dto.auth.ForgotPasswordRequest;
import com.launchwindow.dto.auth.ResetPasswordRequest;
import com.launchwindow.exception.InvalidPasswordResetTokenException;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.PasswordResetToken;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.PasswordResetTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class PasswordResetService {
    private static final int TOKEN_BYTE_LENGTH = 32;

    private final AppUserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetProperties properties;
    private final Clock clock;
    private final SecureRandom secureRandom;

    public PasswordResetService(AppUserRepository userRepository, PasswordResetTokenRepository tokenRepository, PasswordResetMailSender mailSender,
                                PasswordEncoder passwordEncoder, PasswordResetProperties properties, Clock clock) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.clock = clock;
        this.secureRandom = new SecureRandom();
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        userRepository.findByEmailIgnoreCase(email).ifPresent(this::createAndSendToken);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Instant now = clock.instant();
        String tokenHash = hashToken(request.token());

        PasswordResetToken resetToken = tokenRepository.findByTokenHash(tokenHash).orElseThrow(InvalidPasswordResetTokenException::new);

        if (resetToken.isUsed() || resetToken.isExpired(now)) {
            throw new InvalidPasswordResetTokenException();
        }

        String passwordHash = passwordEncoder.encode(request.newPassword());

        resetToken.getUser().updatePasswordHash(passwordHash);
        resetToken.markUsed(now);
    }

    private void createAndSendToken(AppUser user) {
        Instant now = clock.instant();

        if (requestIsWithinCooldown(user, now)) {
            return;
        }

        tokenRepository.deleteAllByUser_Id(user.getId());

        String rawToken = generateToken();
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = new PasswordResetToken(user, tokenHash, now.plus(properties.expiration()));

        tokenRepository.save(resetToken);

        mailSender.sendPasswordResetEmail(user.getEmail(), user.getUsername(), rawToken);
    }

    private boolean requestIsWithinCooldown(AppUser user, Instant now) {
        return tokenRepository.findFirstByUser_IdOrderByCreatedAtDescIdDesc(user.getId())
                .map(PasswordResetToken::getCreatedAt)
                .filter(createdAt -> createdAt != null)
                .map(createdAt -> createdAt.isAfter(now.minus(properties.requestCooldown())))
                .orElse(false);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}