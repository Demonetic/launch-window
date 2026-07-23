package com.launchwindow.dto;

import com.launchwindow.model.AvatarKey;
import com.launchwindow.model.CalendarInvitationStatus;

import java.time.Instant;

public record CalendarInvitationResponse(
        Long id,
        Long launchId,
        String launchName,
        Instant launchTime,
        Long inviterId,
        String inviterUsername,
        AvatarKey inviterAvatarKey,
        String inviterAvatarColor,
        CalendarInvitationStatus status,
        Instant createdAt,
        Instant respondedAt
) {
}