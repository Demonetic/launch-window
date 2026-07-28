package com.launchwindow.dto.notification;

import com.launchwindow.model.AvatarKey;
import com.launchwindow.model.CalendarInvitationStatus;
import com.launchwindow.model.FriendshipStatus;
import com.launchwindow.model.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        NotificationType type,
        boolean read,
        Instant createdAt,
        Long actorId,
        String actorUsername,
        AvatarKey actorAvatarKey,
        String actorAvatarColor,
        Long friendshipId,
        FriendshipStatus friendshipStatus,
        Long calendarInvitationId,
        CalendarInvitationStatus calendarInvitationStatus,
        Long launchId,
        String launchName,
        Instant launchTime
) {
}