package com.launchwindow.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "user_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotification {
    private static final Set<NotificationType> FRIENDSHIP_TYPES =
            EnumSet.of(NotificationType.FRIEND_REQUEST_RECEIVED, NotificationType.FRIEND_REQUEST_ACCEPTED, NotificationType.FRIEND_REQUEST_DECLINED);

    private static final Set<NotificationType> CALENDAR_TYPES =
            EnumSet.of(NotificationType.CALENDAR_INVITATION_RECEIVED, NotificationType.CALENDAR_INVITATION_ACCEPTED, NotificationType.CALENDAR_INVITATION_DECLINED);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private AppUser recipient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private AppUser actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friendship_id")
    private Friendship friendship;

    @Enumerated(EnumType.STRING)
    @Column(name = "friendship_status", length = 20)
    private FriendshipStatus friendshipStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_invitation_id")
    private CalendarInvitation calendarInvitation;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    public static UserNotification forFriendship(AppUser recipient, AppUser actor, NotificationType type, Friendship friendship) {
        if (!FRIENDSHIP_TYPES.contains(type)) {
            throw new IllegalArgumentException("Notification type is not a friendship event");
        }

        Friendship requiredFriendship = Objects.requireNonNull(friendship, "Friendship is required");

        UserNotification notification = new UserNotification();

        notification.initializeUsers(recipient, actor);
        notification.type = type;
        notification.friendship = requiredFriendship;
        notification.friendshipStatus = requiredFriendship.getStatus();

        return notification;
    }

    public static UserNotification forCalendarInvitation(AppUser recipient, AppUser actor, NotificationType type, CalendarInvitation calendarInvitation) {
        if (!CALENDAR_TYPES.contains(type)) {
            throw new IllegalArgumentException("Notification type is not a calendar event");
        }

        UserNotification notification = new UserNotification();

        notification.initializeUsers(recipient, actor);
        notification.type = type;
        notification.calendarInvitation = Objects.requireNonNull(calendarInvitation, "Calendar invitation is required");

        return notification;
    }

    public void resolveFriendship(FriendshipStatus status, Instant readAt) {
        if (!FRIENDSHIP_TYPES.contains(type)) {
            throw new IllegalStateException("Notification is not a friendship event");
        }

        if (status != FriendshipStatus.ACCEPTED && status != FriendshipStatus.DECLINED) {
            throw new IllegalArgumentException("Friendship resolution must be accepted or declined");
        }

        friendshipStatus = status;
        markRead(readAt);
    }

    public void detachFriendship() {
        friendship = null;
    }

    public void markRead(Instant readAt) {
        if (this.readAt == null) {
            this.readAt = Objects.requireNonNull(readAt, "Read time is required");
        }
    }

    public boolean isRead() {
        return readAt != null;
    }

    private void initializeUsers(AppUser recipient, AppUser actor) {
        if (recipient == null || actor == null || recipient.getId() == null || actor.getId() == null) {
            throw new IllegalArgumentException("Notification users must be persisted");
        }

        if (Objects.equals(recipient.getId(), actor.getId())) {
            throw new IllegalArgumentException("Notification recipient and actor must differ");
        }

        this.recipient = recipient;
        this.actor = actor;
    }
}