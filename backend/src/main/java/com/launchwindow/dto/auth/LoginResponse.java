package com.launchwindow.dto.auth;

import com.launchwindow.dto.user.UserResponse;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}