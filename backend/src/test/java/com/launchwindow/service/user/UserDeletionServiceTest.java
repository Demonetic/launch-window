package com.launchwindow.service.user;

import com.launchwindow.dto.DeleteAccountRequest;
import com.launchwindow.exception.InvalidCredentialsException;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.model.AppUser;
import com.launchwindow.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserDeletionServiceTest {

    @Test
    void deleteAccount_correctPasswordDeletesUser() {
        AppUserRepository repository = mock(AppUserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AppUser user = mock(AppUser.class);

        UserDeletionService service = new UserDeletionService(repository, passwordEncoder);
        DeleteAccountRequest request = new DeleteAccountRequest("current-password");

        when(repository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(user.getPasswordHash()).thenReturn("encoded-password");
        when(passwordEncoder.matches("current-password", "encoded-password")).thenReturn(true);

        service.deleteAccount("launch_test", request);

        verify(repository).findByUsername("launch_test");
        verify(passwordEncoder).matches("current-password", "encoded-password");
        verify(repository).delete(user);
    }

    @Test
    void deleteAccount_incorrectPasswordDoesNotDeleteUser() {
        AppUserRepository repository = mock(AppUserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AppUser user = mock(AppUser.class);

        UserDeletionService service = new UserDeletionService(repository, passwordEncoder);

        DeleteAccountRequest request = new DeleteAccountRequest("wrong-password");

        when(repository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(user.getPasswordHash()).thenReturn("encoded-password");
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class, () -> service.deleteAccount("launch_test", request));

        assertEquals("Password is incorrect", exception.getMessage());

        verify(repository, never()).delete(any());
    }

    @Test
    void deleteAccount_missingUserThrowsResourceNotFound() {
        AppUserRepository repository = mock(AppUserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        UserDeletionService service = new UserDeletionService(repository, passwordEncoder);
        DeleteAccountRequest request = new DeleteAccountRequest("password");

        when(repository.findByUsername("missing")).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> service.deleteAccount("missing", request));

        assertEquals(
                "Authenticated user was not found",
                exception.getMessage()
        );

        verifyNoInteractions(passwordEncoder);
        verify(repository, never()).delete(any());
    }
}