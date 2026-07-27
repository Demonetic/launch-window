package com.launchwindow.service.notification;

import com.launchwindow.dto.NotificationResponse;
import com.launchwindow.dto.UnreadNotificationCountResponse;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.model.*;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.UserNotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-27T13:00:00Z");

    @Test
    void getLatestMapsFriendshipNotification() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        UserNotificationRepository notificationRepository = mock(UserNotificationRepository.class);

        NotificationService service = createService(userRepository, notificationRepository);

        UserNotification notification = mock(UserNotification.class);
        AppUser actor = actor();
        Friendship friendship = mock(Friendship.class);

        when(notification.getId()).thenReturn(10L);
        when(notification.getType()).thenReturn(NotificationType.FRIEND_REQUEST_RECEIVED);
        when(notification.isRead()).thenReturn(false);
        when(notification.getCreatedAt()).thenReturn(Instant.parse("2026-07-27T12:00:00Z"));
        when(notification.getActor()).thenReturn(actor);
        when(notification.getFriendship()).thenReturn(friendship);
        when(notification.getFriendshipStatus()).thenReturn(FriendshipStatus.PENDING);
        when(notification.getCalendarInvitation()).thenReturn(null);
        when(friendship.getId()).thenReturn(20L);
        when(notificationRepository.findLatestForRecipient("anna", PageRequest.of(0, 50))).thenReturn(List.of(notification));

        List<NotificationResponse> result = service.getLatest("anna");

        assertEquals(1, result.size());

        NotificationResponse response = result.getFirst();

        assertEquals(10L, response.id());
        assertEquals(NotificationType.FRIEND_REQUEST_RECEIVED, response.type());
        assertFalse(response.read());
        assertEquals(2L, response.actorId());
        assertEquals("alex", response.actorUsername());
        assertEquals(AvatarKey.ALIEN, response.actorAvatarKey());
        assertEquals("#8FE8C3", response.actorAvatarColor());
        assertEquals(20L, response.friendshipId());
        assertEquals(FriendshipStatus.PENDING, response.friendshipStatus());
        assertNull(response.calendarInvitationId());
        assertNull(response.launchId());
    }

    @Test
    void getLatestMapsCalendarInvitationNotification() {
        UserNotificationRepository notificationRepository = mock(UserNotificationRepository.class);

        NotificationService service = createService(mock(AppUserRepository.class), notificationRepository);

        UserNotification notification = mock(UserNotification.class);
        AppUser actor = actor();
        CalendarInvitation invitation = mock(CalendarInvitation.class);
        CalendarEntry calendarEntry = mock(CalendarEntry.class);
        Launch launch = mock(Launch.class);

        when(notification.getId()).thenReturn(11L);
        when(notification.getType()).thenReturn(NotificationType.CALENDAR_INVITATION_ACCEPTED);
        when(notification.isRead()).thenReturn(true);
        when(notification.getCreatedAt()).thenReturn(Instant.parse("2026-07-27T12:30:00Z"));
        when(notification.getActor()).thenReturn(actor);
        when(notification.getFriendship()).thenReturn(null);
        when(notification.getCalendarInvitation()).thenReturn(invitation);
        when(invitation.getId()).thenReturn(30L);
        when(invitation.getStatus()).thenReturn(CalendarInvitationStatus.ACCEPTED);
        when(invitation.getCalendarEntry()).thenReturn(calendarEntry);
        when(calendarEntry.getLaunch()).thenReturn(launch);
        when(launch.getId()).thenReturn(40L);
        when(launch.getName()).thenReturn("Test launch");
        when(launch.getLaunchTime()).thenReturn(Instant.parse("2026-08-01T10:00:00Z"));
        when(notificationRepository.findLatestForRecipient("anna", PageRequest.of(0, 50))).thenReturn(List.of(notification));

        NotificationResponse response = service.getLatest("anna").getFirst();

        assertEquals(NotificationType.CALENDAR_INVITATION_ACCEPTED, response.type());
        assertTrue(response.read());
        assertNull(response.friendshipId());
        assertEquals(30L, response.calendarInvitationId());
        assertEquals(CalendarInvitationStatus.ACCEPTED, response.calendarInvitationStatus());
        assertEquals(40L, response.launchId());
        assertEquals("Test launch", response.launchName());
        assertEquals(Instant.parse("2026-08-01T10:00:00Z"), response.launchTime());
    }

    @Test
    void getUnreadCountReturnsRepositoryCount() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        UserNotificationRepository notificationRepository = mock(UserNotificationRepository.class);

        NotificationService service = createService(userRepository, notificationRepository);

        AppUser user = mock(AppUser.class);

        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(user));
        when(notificationRepository.countByRecipient_IdAndReadAtIsNull(1L)).thenReturn(4L);

        UnreadNotificationCountResponse response = service.getUnreadCount("anna");

        assertEquals(4L, response.unreadCount());
    }

    @Test
    void markReadMarksOwnedNotification() {
        UserNotificationRepository notificationRepository = mock(UserNotificationRepository.class);

        NotificationService service = createService(mock(AppUserRepository.class), notificationRepository);

        UserNotification notification = mock(UserNotification.class);

        when(notificationRepository.findForRecipient(10L, "anna")).thenReturn(Optional.of(notification));

        service.markRead("anna", 10L);

        verify(notification).markRead(CURRENT_TIME);
    }

    @Test
    void markReadRejectsUnknownOrAnotherUsersNotification() {
        UserNotificationRepository notificationRepository = mock(UserNotificationRepository.class);

        NotificationService service = createService(mock(AppUserRepository.class), notificationRepository);

        when(notificationRepository.findForRecipient(10L, "anna")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () -> service.markRead("anna", 10L));

        assertEquals("Notification was not found", exception.getMessage());
    }

    @Test
    void markAllReadUsesSameTimestampForEveryNotification() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        UserNotificationRepository notificationRepository = mock(UserNotificationRepository.class);

        NotificationService service = createService(userRepository, notificationRepository);

        AppUser user = mock(AppUser.class);
        UserNotification first = mock(UserNotification.class);
        UserNotification second = mock(UserNotification.class);

        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(user));
        when(notificationRepository.findAllByRecipient_IdAndReadAtIsNull(1L)).thenReturn(List.of(first, second));

        service.markAllRead("anna");

        verify(first).markRead(CURRENT_TIME);
        verify(second).markRead(CURRENT_TIME);
    }

    @Test
    void unreadCountRejectsMissingAuthenticatedUser() {
        AppUserRepository userRepository = mock(AppUserRepository.class);

        NotificationService service = createService(userRepository, mock(UserNotificationRepository.class));

        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () -> service.getUnreadCount("missing"));

        assertEquals("Authenticated user was not found", exception.getMessage());
    }

    private NotificationService createService(AppUserRepository userRepository, UserNotificationRepository notificationRepository) {
        return new NotificationService(userRepository, notificationRepository, Clock.fixed(CURRENT_TIME, ZoneOffset.UTC));
    }

    private AppUser actor() {
        AppUser actor = mock(AppUser.class);

        when(actor.getId()).thenReturn(2L);
        when(actor.getUsername()).thenReturn("alex");
        when(actor.getAvatarKey()).thenReturn(AvatarKey.ALIEN);
        when(actor.getAvatarColor()).thenReturn("#8FE8C3");

        return actor;
    }
}