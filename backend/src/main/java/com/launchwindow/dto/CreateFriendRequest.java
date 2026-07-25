package com.launchwindow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFriendRequest(
        @NotBlank(message = "Username or email is required")
        @Size(
                max = 255,
                message = "Username or email must not exceed 255 characters"
        )
        String identifier
) {
}