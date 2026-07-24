package com.launchwindow.dto;

import com.launchwindow.model.AvatarKey;

import java.time.Instant;

public record LaunchNoteOverviewResponse(
        Long id,
        Long launchId,
        String launchName,
        Instant launchTime,
        String organizationName,
        String imageUrl,
        Long authorId,
        String authorUsername,
        AvatarKey authorAvatarKey,
        String authorAvatarColor,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
}