package com.launchwindow.service.auth;

import com.launchwindow.config.PasswordResetProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpPasswordResetMailSender implements PasswordResetMailSender {

    private final JavaMailSender mailSender;
    private final PasswordResetProperties properties;

    public SmtpPasswordResetMailSender(JavaMailSender mailSender, PasswordResetProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void sendPasswordResetEmail(String recipientEmail, String username, String rawToken) {
        String resetUrl = normalizedFrontendBaseUrl() + "/reset-password?token=" + rawToken;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(properties.fromEmail());
        message.setTo(recipientEmail);
        message.setSubject("Reset your Launch Window password");
        message.setText("""
                Hello %s,

                We received a request to reset your Launch Window password.

                Open the following link to choose a new password:

                %s

                This link expires in 30 minutes and can only be used once.

                If you did not request a password reset, you can ignore this email.

                Launch Window
                """.formatted(username, resetUrl));

        mailSender.send(message);
    }

    private String normalizedFrontendBaseUrl() {
        return properties.frontendBaseUrl().replaceAll("/+$", "");
    }
}