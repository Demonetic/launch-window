package com.launchwindow.service.user;

import com.launchwindow.dto.UserResponse;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.Role;
import com.launchwindow.repository.AppUserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserQueryServiceTest {

    @Test
    void shouldReturnUserByUsername() {
        AppUserRepository repository = mock(AppUserRepository.class);
        AppUser user = mock(AppUser.class);
        UserQueryService service = new UserQueryService(repository);

        when(repository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("launch_test");
        when(user.getEmail()).thenReturn("launch-test@example.com");
        when(user.getRole()).thenReturn(Role.USER);

        Optional<UserResponse> result = service.getUser("launch_test");

        UserResponse expected = new UserResponse(
                1L,
                "launch_test",
                "launch-test@example.com",
                Role.USER
        );

        assertEquals(Optional.of(expected), result);
    }

    @Test
    void shouldReturnEmptyForUnknownUsername() {
        AppUserRepository repository = mock(AppUserRepository.class);
        UserQueryService service = new UserQueryService(repository);

        when(repository.findByUsername("missing")).thenReturn(Optional.empty());

        assertEquals(Optional.empty(), service.getUser("missing"));
    }
}