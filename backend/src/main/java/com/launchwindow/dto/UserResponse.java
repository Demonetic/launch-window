package com.launchwindow.dto;

import com.launchwindow.model.AvatarKey;
import com.launchwindow.model.Role;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        AvatarKey avatarKey,
        String avatarColor
) {
}