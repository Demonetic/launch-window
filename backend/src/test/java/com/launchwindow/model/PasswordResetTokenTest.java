package com.launchwindow.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-24T12:00:00Z");

    @Test
    void tokenBeforeExpirationIsNotExpired() {
        PasswordResetToken token =
                new PasswordResetToken(null, "token-hash", CURRENT_TIME.plusSeconds(60));

        assertFalse(token.isExpired(CURRENT_TIME));
    }

    @Test
    void tokenAtExpirationTimeIsExpired() {
        PasswordResetToken token = new PasswordResetToken(null, "token-hash", CURRENT_TIME);

        assertTrue(token.isExpired(CURRENT_TIME));
    }

    @Test
    void markUsedStoresUsageTime() {
        PasswordResetToken token =
                new PasswordResetToken(null, "token-hash", CURRENT_TIME.plusSeconds(60));

        assertFalse(token.isUsed());

        token.markUsed(CURRENT_TIME);

        assertTrue(token.isUsed());
        assertEquals(CURRENT_TIME, token.getUsedAt());
    }
}