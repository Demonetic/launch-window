package com.launchwindow.dto;

import com.launchwindow.model.AvatarKey;

import java.time.Instant;

public record LaunchNoteResponse(
        Long id,
        Long launchId,
        Long authorId,
        String authorUsername,
        AvatarKey authorAvatarKey,
        String authorAvatarColor,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
}