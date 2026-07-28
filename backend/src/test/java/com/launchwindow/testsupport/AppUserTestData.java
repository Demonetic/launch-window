package com.launchwindow.testsupport;

import com.launchwindow.model.AppUser;
import com.launchwindow.model.AvatarKey;
import com.launchwindow.model.Role;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AppUserTestData {
    private static final String DEFAULT_PASSWORD_HASH = "password-hash";

    private AppUserTestData() {
    }

    public static AppUser user(String username, String email) {
        return user(username, email, DEFAULT_PASSWORD_HASH);
    }

    public static AppUser user(String username, String email, String passwordHash) {
        return new AppUser(username, email, passwordHash, Role.USER);
    }

    public static AppUser persistedUser(Long id) {
        AppUser user = mock(AppUser.class);

        when(user.getId()).thenReturn(id);

        return user;
    }

    public static AppUser persistedUser(Long id, String username, AvatarKey avatarKey) {
        AppUser user = persistedUser(id);

        when(user.getUsername()).thenReturn(username);
        when(user.getAvatarKey()).thenReturn(avatarKey);

        return user;
    }

    public static AppUser persistedUser(Long id, String username, AvatarKey avatarKey, String avatarColor) {
        AppUser user = persistedUser(id, username, avatarKey);

        when(user.getAvatarColor()).thenReturn(avatarColor);

        return user;
    }
}
