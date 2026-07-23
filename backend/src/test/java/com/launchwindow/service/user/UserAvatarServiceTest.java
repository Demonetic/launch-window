package com.launchwindow.service.user;

import com.launchwindow.dto.UpdateAvatarRequest;
import com.launchwindow.dto.UserResponse;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.AvatarKey;
import com.launchwindow.model.Role;
import com.launchwindow.repository.AppUserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserAvatarServiceTest {

    @Test
    void updateAvatar_updatesUserAndReturnsResponse() {
        AppUserRepository repository = mock(AppUserRepository.class);
        AppUser user = mock(AppUser.class);
        UserAvatarService service = new UserAvatarService(repository);

        UpdateAvatarRequest request = new UpdateAvatarRequest(AvatarKey.ALIEN, "#8b99ff");

        when(repository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("launch_test");
        when(user.getEmail()).thenReturn("launch-test@example.com");
        when(user.getRole()).thenReturn(Role.USER);
        when(user.getAvatarKey()).thenReturn(AvatarKey.ALIEN);
        when(user.getAvatarColor()).thenReturn("#8B99FF");

        UserResponse result = service.updateAvatar("launch_test", request);

        UserResponse expected = new UserResponse(
                1L,
                "launch_test",
                "launch-test@example.com",
                Role.USER,
                AvatarKey.ALIEN,
                "#8B99FF"
        );

        assertEquals(expected, result);

        verify(repository).findByUsername("launch_test");
        verify(user).updateAvatar(AvatarKey.ALIEN, "#8B99FF");
    }

    @Test
    void updateAvatar_normalizesColorToUppercase() {
        AppUserRepository repository = mock(AppUserRepository.class);
        AppUser user = mock(AppUser.class);
        UserAvatarService service = new UserAvatarService(repository);

        UpdateAvatarRequest request = new UpdateAvatarRequest(AvatarKey.PLANET, "#a1b2c3");

        when(repository.findByUsername("launch_test")).thenReturn(Optional.of(user));

        service.updateAvatar("launch_test", request);

        verify(user).updateAvatar(AvatarKey.PLANET, "#A1B2C3");
    }

    @Test
    void updateAvatar_unknownUserThrowsResourceNotFound() {
        AppUserRepository repository = mock(AppUserRepository.class);
        UserAvatarService service = new UserAvatarService(repository);

        UpdateAvatarRequest request = new UpdateAvatarRequest(AvatarKey.ASTRONAUT, "#FFFFFF");

        when(repository.findByUsername("missing")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () -> service.updateAvatar("missing", request));

        assertEquals("Authenticated user was not found", exception.getMessage());

        verify(repository).findByUsername("missing");
    }
}