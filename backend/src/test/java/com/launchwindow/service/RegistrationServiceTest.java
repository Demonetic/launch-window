package com.launchwindow.service;

import com.launchwindow.dto.RegisterRequest;
import com.launchwindow.dto.UserResponse;
import com.launchwindow.exception.UserAlreadyExistsException;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.Role;
import com.launchwindow.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
    @Mock
    private AppUserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegistrationService service;

    @BeforeEach
    void setUp() {
        service = new RegistrationService(repository, passwordEncoder);
    }

    @Test
    void shouldNormalizeAndRegisterUserWIthHashedPassword() {
        RegisterRequest request = new RegisterRequest(
                " Anna_Dev ",
                " ANNA@EXAMPLE.COM ",
                "password123");

        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(repository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = service.register(request);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(captor.capture());

        AppUser savedUser = captor.getValue();

        assertAll(
                () -> assertEquals("Anna_Dev", savedUser.getUsername()),
                () -> assertEquals("anna@example.com", savedUser.getEmail()),
                () -> assertEquals("hashed-password", savedUser.getPasswordHash()),
                () -> assertEquals(Role.USER, savedUser.getRole()),
                () -> assertEquals("Anna_Dev", response.username()),
                () -> assertEquals("anna@example.com", response.email())
        );
    }

    @Test
    void shouldRejectDuplicateUsername() {
        RegisterRequest request = request();

        when(repository.existsByEmail("anna@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> service.register(request));

        verifyNoInteractions(passwordEncoder);
        verify(repository, never()).save(any());
    }

    private RegisterRequest request() {
        return new RegisterRequest("anna", "anna@example.com", "password123");
    }
}