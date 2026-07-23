package com.launchwindow.dto;

import com.launchwindow.model.AvatarKey;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateAvatarRequest(
        @NotNull
        AvatarKey avatarKey,

        @NotNull
        @Pattern(
                regexp = "^#[0-9A-Fa-f]{6}$",
                message = "Avatar color must be a valid hex color"
        )
        String avatarColor
) {
}