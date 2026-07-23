package com.launchwindow.dto;

import com.launchwindow.model.AvatarKey;

public record CalendarParticipantResponse(
        Long userId,
        String username,
        AvatarKey avatarKey,
        String avatarColor
) {
}