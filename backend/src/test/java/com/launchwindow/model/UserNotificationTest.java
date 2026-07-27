package com.launchwindow.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserNotificationTest {
    private static final Instant READ_TIME = Instant.parse("2026-07-27T09:00:00Z");

    @Test
    void friendshipNotificationStoresRelationship() {
        AppUser recipient = user(1L);
        AppUser actor = user(2L);
        Friendship friendship = mock(Friendship.class);

        UserNotification notification =
                UserNotification.forFriendship(recipient, actor, NotificationType.FRIEND_REQUEST_RECEIVED, friendship);

        assertEquals(recipient, notification.getRecipient());
        assertEquals(actor, notification.getActor());
        assertEquals(NotificationType.FRIEND_REQUEST_RECEIVED, notification.getType());
        assertEquals(friendship, notification.getFriendship());
        assertNull(notification.getCalendarInvitation());
        assertFalse(notification.isRead());
    }

    @Test
    void calendarNotificationStoresInvitation() {
        AppUser recipient = user(1L);
        AppUser actor = user(2L);

        CalendarInvitation invitation = mock(CalendarInvitation.class);

        UserNotification notification = UserNotification
                        .forCalendarInvitation(recipient, actor, NotificationType.CALENDAR_INVITATION_RECEIVED, invitation);

        assertEquals(invitation, notification.getCalendarInvitation());
        assertNull(notification.getFriendship());
    }

    @Test
    void markReadSetsTimeOnlyOnce() {
        UserNotification notification =
                UserNotification.forFriendship(user(1L), user(2L), NotificationType.FRIEND_REQUEST_RECEIVED, mock(Friendship.class));

        notification.markRead(READ_TIME);
        notification.markRead(READ_TIME.plusSeconds(60));

        assertTrue(notification.isRead());
        assertEquals(READ_TIME, notification.getReadAt());
    }

    @Test
    void friendshipFactoryRejectsCalendarType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> UserNotification.forFriendship(user(1L), user(2L), NotificationType.CALENDAR_INVITATION_RECEIVED, mock(Friendship.class))
        );
    }

    @Test
    void calendarFactoryRejectsFriendshipType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> UserNotification.forCalendarInvitation(user(1L), user(2L), NotificationType.FRIEND_REQUEST_ACCEPTED, mock(CalendarInvitation.class))
        );
    }

    @Test
    void notificationRejectsSameUser() {
        AppUser user = user(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> UserNotification.forFriendship(user, user, NotificationType.FRIEND_REQUEST_RECEIVED, mock(Friendship.class))
        );
    }

    private AppUser user(Long id) {
        AppUser user = mock(AppUser.class);
        when(user.getId()).thenReturn(id);
        return user;
    }
}