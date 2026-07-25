package com.launchwindow.service.auth;

import com.launchwindow.config.PasswordResetProperties;
import com.launchwindow.dto.ForgotPasswordRequest;
import com.launchwindow.dto.ResetPasswordRequest;
import com.launchwindow.exception.InvalidPasswordResetTokenException;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.PasswordResetToken;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-25T08:00:00Z");

    private static final PasswordResetProperties PROPERTIES = new PasswordResetProperties("http://localhost:5173",
            Duration.ofMinutes(30), Duration.ofMinutes(1), "no-reply@launch-window.local");

    @Test
    void requestPasswordReset_existingUserCreatesTokenAndSendsEmail() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        PasswordResetTokenRepository tokenRepository = mock(PasswordResetTokenRepository.class);
        PasswordResetMailSender mailSender = mock(PasswordResetMailSender.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AppUser user = mock(AppUser.class);

        PasswordResetService service = service(userRepository, tokenRepository, mailSender, passwordEncoder);

        when(userRepository.findByEmailIgnoreCase("space@example.com")).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn("space@example.com");
        when(user.getUsername()).thenReturn("spacefan");
        when(tokenRepository.findFirstByUser_IdOrderByCreatedAtDescIdDesc(1L)).thenReturn(Optional.empty());

        service.requestPasswordReset(new ForgotPasswordRequest(" SPACE@EXAMPLE.COM "));

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<String> rawTokenCaptor = ArgumentCaptor.forClass(String.class);

        verify(tokenRepository).deleteAllByUser_Id(1L);
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(mailSender).sendPasswordResetEmail(eq("space@example.com"), eq("spacefan"), rawTokenCaptor.capture());

        PasswordResetToken storedToken = tokenCaptor.getValue();
        String rawToken = rawTokenCaptor.getValue();

        assertFalse(rawToken.isBlank());
        assertNotEquals(rawToken, storedToken.getTokenHash());
        assertEquals(hashToken(rawToken), storedToken.getTokenHash());
        assertEquals(CURRENT_TIME.plus(Duration.ofMinutes(30)), storedToken.getExpiresAt());
        assertEquals(user, storedToken.getUser());
    }

    @Test
    void requestPasswordReset_unknownEmailDoesNothing() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        PasswordResetTokenRepository tokenRepository = mock(PasswordResetTokenRepository.class);
        PasswordResetMailSender mailSender = mock(PasswordResetMailSender.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        PasswordResetService service = service(userRepository, tokenRepository, mailSender, passwordEncoder);

        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        service.requestPasswordReset(new ForgotPasswordRequest("missing@example.com"));

        verifyNoInteractions(tokenRepository, mailSender, passwordEncoder);
    }

    @Test
    void requestPasswordReset_duringCooldownDoesNotSendAgain() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        PasswordResetTokenRepository tokenRepository = mock(PasswordResetTokenRepository.class);
        PasswordResetMailSender mailSender = mock(PasswordResetMailSender.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AppUser user = mock(AppUser.class);
        PasswordResetToken existingToken = mock(PasswordResetToken.class);

        PasswordResetService service = service(userRepository, tokenRepository, mailSender, passwordEncoder);

        when(userRepository.findByEmailIgnoreCase("space@example.com")).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(1L);
        when(tokenRepository.findFirstByUser_IdOrderByCreatedAtDescIdDesc(1L)).thenReturn(Optional.of(existingToken));
        when(existingToken.getCreatedAt()).thenReturn(CURRENT_TIME.minusSeconds(30));

        service.requestPasswordReset(new ForgotPasswordRequest("space@example.com"));

        verify(tokenRepository, never()).deleteAllByUser_Id(anyLong());
        verify(tokenRepository, never()).save(any());
        verifyNoInteractions(mailSender);
    }

    @Test
    void resetPassword_validTokenChangesPasswordAndMarksTokenUsed() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        PasswordResetTokenRepository tokenRepository = mock(PasswordResetTokenRepository.class);
        PasswordResetMailSender mailSender = mock(PasswordResetMailSender.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordResetToken resetToken = mock(PasswordResetToken.class);
        AppUser user = mock(AppUser.class);

        PasswordResetService service = service(userRepository, tokenRepository, mailSender, passwordEncoder);

        String rawToken = "valid-reset-token";

        when(tokenRepository.findByTokenHash(hashToken(rawToken))).thenReturn(Optional.of(resetToken));
        when(resetToken.isUsed()).thenReturn(false);
        when(resetToken.isExpired(CURRENT_TIME)).thenReturn(false);
        when(resetToken.getUser()).thenReturn(user);
        when(passwordEncoder.encode("new-password")).thenReturn("new-password-hash");

        service.resetPassword(new ResetPasswordRequest(rawToken, "new-password"));

        verify(passwordEncoder).encode("new-password");
        verify(user).updatePasswordHash("new-password-hash");
        verify(resetToken).markUsed(CURRENT_TIME);
    }

    @Test
    void resetPassword_unknownTokenThrowsException() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        PasswordResetTokenRepository tokenRepository = mock(PasswordResetTokenRepository.class);
        PasswordResetMailSender mailSender = mock(PasswordResetMailSender.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        PasswordResetService service = service(userRepository, tokenRepository, mailSender, passwordEncoder);

        String rawToken = "unknown-token";

        when(tokenRepository.findByTokenHash(hashToken(rawToken))).thenReturn(Optional.empty());

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> service.resetPassword(new ResetPasswordRequest(rawToken, "new-password")));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void resetPassword_expiredTokenThrowsException() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        PasswordResetTokenRepository tokenRepository = mock(PasswordResetTokenRepository.class);
        PasswordResetMailSender mailSender = mock(PasswordResetMailSender.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordResetToken resetToken = mock(PasswordResetToken.class);

        PasswordResetService service = service(userRepository, tokenRepository, mailSender, passwordEncoder);

        String rawToken = "expired-token";

        when(tokenRepository.findByTokenHash(hashToken(rawToken))).thenReturn(Optional.of(resetToken));
        when(resetToken.isUsed()).thenReturn(false);
        when(resetToken.isExpired(CURRENT_TIME)).thenReturn(true);

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> service.resetPassword(new ResetPasswordRequest(rawToken, "new-password")));

        verifyNoInteractions(passwordEncoder);
        verify(resetToken, never()).markUsed(any());
    }

    @Test
    void resetPassword_usedTokenThrowsException() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        PasswordResetTokenRepository tokenRepository = mock(PasswordResetTokenRepository.class);
        PasswordResetMailSender mailSender = mock(PasswordResetMailSender.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordResetToken resetToken = mock(PasswordResetToken.class);

        PasswordResetService service = service(userRepository, tokenRepository, mailSender, passwordEncoder);

        String rawToken = "used-token";

        when(tokenRepository.findByTokenHash(hashToken(rawToken))).thenReturn(Optional.of(resetToken));
        when(resetToken.isUsed()).thenReturn(true);

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> service.resetPassword(new ResetPasswordRequest(rawToken, "new-password")));

        verify(resetToken, never()).isExpired(any());
        verifyNoInteractions(passwordEncoder);
    }

    private PasswordResetService service(AppUserRepository userRepository, PasswordResetTokenRepository tokenRepository,
                                         PasswordResetMailSender mailSender, PasswordEncoder passwordEncoder) {
        return new PasswordResetService(
                userRepository,
                tokenRepository,
                mailSender,
                passwordEncoder,
                PROPERTIES,
                Clock.fixed(CURRENT_TIME, ZoneOffset.UTC)
        );
    }

    private static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}