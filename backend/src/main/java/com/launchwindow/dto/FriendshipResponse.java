package com.launchwindow.dto;

import com.launchwindow.model.AvatarKey;
import com.launchwindow.model.FriendshipStatus;

import java.time.Instant;

public record FriendshipResponse(
        Long id,
        Long userId,
        String username,
        AvatarKey avatarKey,
        String avatarColor,
        FriendshipStatus status,
        boolean requestedByCurrentUser,
        Instant createdAt,
        Instant respondedAt
) {
}