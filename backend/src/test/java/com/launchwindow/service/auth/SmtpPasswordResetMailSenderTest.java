package com.launchwindow.service.auth;

import com.launchwindow.config.properties.PasswordResetProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SmtpPasswordResetMailSenderTest {

    @Test
    void sendPasswordResetEmail_buildsExpectedMessage() {
        JavaMailSender mailSender = mock(JavaMailSender.class);

        PasswordResetProperties properties = new PasswordResetProperties("http://localhost:5173/", Duration.ofMinutes(30),
                Duration.ofMinutes(1), "no-reply@launch-window.local");

        SmtpPasswordResetMailSender sender = new SmtpPasswordResetMailSender(mailSender, properties);

        sender.sendPasswordResetEmail("space@example.com", "spacefan", "raw-reset-token");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals("no-reply@launch-window.local", message.getFrom());
        assertArrayEquals(new String[]{"space@example.com"}, message.getTo());
        assertEquals("Reset your Launch Window password", message.getSubject());
        assertNotNull(message.getText());
        assertTrue(message.getText().contains("Hello spacefan"));
        assertTrue(message.getText().contains("http://localhost:5173/reset-password" + "?token=raw-reset-token"));
    }
}