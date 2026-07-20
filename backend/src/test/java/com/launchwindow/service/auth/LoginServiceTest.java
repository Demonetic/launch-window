package com.launchwindow.service.auth;

import com.launchwindow.config.JwtProperties;
import com.launchwindow.dto.LoginRequest;
import com.launchwindow.dto.LoginResponse;
import com.launchwindow.exception.InvalidCredentialsException;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.Role;
import com.launchwindow.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    @Mock
    private AppUserRepository repository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    private LoginService service;
    private AppUser user;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties("test-secret-with-at-least-32-characters", "launch-window",
                Duration.ofHours(2));

        service = new LoginService(repository, passwordEncoder, jwtService, properties);
        user = new AppUser("launch_test", "launch-test@example.com", "hashed-password", Role.USER);
    }

    @Test
    void shouldReturnTokenForValidUsername() {
        when(repository.findByUsernameIgnoreCaseOrEmailIgnoreCase("LAUNCH_TEST", "launch_test"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("TestPassword123!", "hashed-password"))
                .thenReturn(true);
        when(jwtService.createToken(user)).thenReturn("signed-token");

        LoginResponse response = service.login(new LoginRequest(" LAUNCH_TEST ", "TestPassword123!"));

        assertAll(() -> assertEquals("signed-token", response.accessToken()),
                () -> assertEquals("Bearer", response.tokenType()),
                () -> assertEquals(7200, response.expiresIn()),
                () -> assertEquals("launch_test", response.user().username()),
                () -> assertEquals(Role.USER, response.user().role()));

        verify(repository).findByUsernameIgnoreCaseOrEmailIgnoreCase("LAUNCH_TEST", "launch_test");
        verify(jwtService).createToken(user);
    }

    @Test
    void shouldReturnTokenForValidEmail() {
        when(repository.findByUsernameIgnoreCaseOrEmailIgnoreCase("LAUNCH-TEST@EXAMPLE.COM",
                "launch-test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("TestPassword123!", "hashed-password"))
                .thenReturn(true);
        when(jwtService.createToken(user)).thenReturn("signed-token");

        LoginResponse response = service.login(
                new LoginRequest(" LAUNCH-TEST@EXAMPLE.COM ", "TestPassword123!"));

        assertAll(() -> assertEquals("signed-token", response.accessToken()),
                () -> assertEquals("launch_test", response.user().username()),
                () -> assertEquals("launch-test@example.com", response.user().email()));

        verify(repository).findByUsernameIgnoreCaseOrEmailIgnoreCase("LAUNCH-TEST@EXAMPLE.COM",
                        "launch-test@example.com");
        verify(jwtService).createToken(user);
    }

    @Test
    void shouldRejectIncorrectPassword() {
        when(repository.findByUsernameIgnoreCaseOrEmailIgnoreCase("launch_test", "launch_test"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password"))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class, () -> service.login(
                        new LoginRequest("launch_test", "wrong-password")));

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldRejectUnknownIdentifier() {
        when(repository.findByUsernameIgnoreCaseOrEmailIgnoreCase("missing", "missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class, () -> service.login(
                        new LoginRequest("missing", "password123")));

        verifyNoInteractions(passwordEncoder, jwtService);
    }
}