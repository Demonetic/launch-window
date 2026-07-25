package com.launchwindow.service.auth;

public interface PasswordResetMailSender {

    void sendPasswordResetEmail(
            String recipientEmail,
            String username,
            String rawToken
    );
}