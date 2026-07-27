package com.launchwindow.service.user;

import com.launchwindow.dto.FriendshipResponse;
import com.launchwindow.exception.InvalidFriendshipException;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.AvatarKey;
import com.launchwindow.model.Friendship;
import com.launchwindow.model.FriendshipStatus;
import com.launchwindow.model.NotificationType;
import com.launchwindow.model.UserNotification;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.FriendshipRepository;
import com.launchwindow.repository.UserNotificationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FriendshipCommandServiceTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-25T18:00:00Z");

    @Test
    void acceptAcceptsRequestAndCreatesNotifications() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);
        UserNotificationRepository notificationRepository = mock(UserNotificationRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository, notificationRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);
        AppUser alex = user(2L, "alex", AvatarKey.ALIEN);
        Friendship friendship = mock(Friendship.class);
        UserNotification receivedNotification = mock(UserNotification.class);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));
        when(friendshipRepository.findForUser(10L, 1L)).thenReturn(Optional.of(friendship));
        when(friendship.getStatus()).thenReturn(FriendshipStatus.PENDING, FriendshipStatus.ACCEPTED);
        when(friendship.wasRequestedBy(1L)).thenReturn(false);
        when(friendship.getOtherUser(1L)).thenReturn(alex);
        when(friendship.getRequester()).thenReturn(alex);

        stubResponseFields(friendship);

        when(notificationRepository.findByRecipient_IdAndFriendship_IdAndType(1L, 10L, NotificationType.FRIEND_REQUEST_RECEIVED))
                .thenReturn(Optional.of(receivedNotification));

        FriendshipResponse result = service.accept("anna", 10L);

        verify(friendship).accept(CURRENT_TIME);
        verify(receivedNotification).markRead(CURRENT_TIME);

        assertEquals(FriendshipStatus.ACCEPTED, result.status());
        assertEquals(CURRENT_TIME, result.respondedAt());

        ArgumentCaptor<UserNotification> notificationCaptor = ArgumentCaptor.forClass(UserNotification.class);

        verify(notificationRepository).save(notificationCaptor.capture());

        UserNotification notification = notificationCaptor.getValue();

        assertEquals(alex, notification.getRecipient());
        assertEquals(anna, notification.getActor());
        assertEquals(NotificationType.FRIEND_REQUEST_ACCEPTED, notification.getType());
        assertEquals(friendship, notification.getFriendship());
    }

    @Test
    void declineDeclinesRequestAndCreatesNotifications() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);
        UserNotificationRepository notificationRepository = mock(UserNotificationRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository, notificationRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);
        AppUser alex = user(2L, "alex", AvatarKey.ALIEN);
        Friendship friendship = mock(Friendship.class);
        UserNotification receivedNotification = mock(UserNotification.class);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));
        when(friendshipRepository.findForUser(10L, 1L)).thenReturn(Optional.of(friendship));
        when(friendship.getStatus()).thenReturn(FriendshipStatus.PENDING, FriendshipStatus.DECLINED);
        when(friendship.wasRequestedBy(1L)).thenReturn(false);
        when(friendship.getOtherUser(1L)).thenReturn(alex);
        when(friendship.getRequester()).thenReturn(alex);

        stubResponseFields(friendship);

        when(notificationRepository.findByRecipient_IdAndFriendship_IdAndType(1L, 10L, NotificationType.FRIEND_REQUEST_RECEIVED))
                .thenReturn(Optional.of(receivedNotification));

        FriendshipResponse result = service.decline("anna", 10L);

        verify(friendship).decline(CURRENT_TIME);
        verify(receivedNotification).markRead(CURRENT_TIME);

        assertEquals(FriendshipStatus.DECLINED, result.status());

        ArgumentCaptor<UserNotification> notificationCaptor = ArgumentCaptor.forClass(UserNotification.class);

        verify(notificationRepository).save(notificationCaptor.capture());

        UserNotification notification = notificationCaptor.getValue();

        assertEquals(alex, notification.getRecipient());
        assertEquals(anna, notification.getActor());
        assertEquals(NotificationType.FRIEND_REQUEST_DECLINED, notification.getType());
        assertEquals(friendship, notification.getFriendship());
    }

    @Test
    void acceptRejectsOwnOutgoingRequest() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);
        UserNotificationRepository notificationRepository = mock(UserNotificationRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository, notificationRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);
        Friendship friendship = mock(Friendship.class);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));
        when(friendshipRepository.findForUser(10L, 1L)).thenReturn(Optional.of(friendship));
        when(friendship.getStatus()).thenReturn(FriendshipStatus.PENDING);
        when(friendship.wasRequestedBy(1L)).thenReturn(true);

        InvalidFriendshipException exception = assertThrows(InvalidFriendshipException.class, () -> service.accept("anna", 10L));

        assertEquals("You cannot answer your own friend request", exception.getMessage());

        verify(friendship, never()).accept(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void removeDeletesFriendshipForMember() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository, mock(UserNotificationRepository.class));

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);
        Friendship friendship = mock(Friendship.class);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));
        when(friendshipRepository.findForUser(10L, 1L)).thenReturn(Optional.of(friendship));

        service.remove("anna", 10L);

        verify(friendshipRepository).delete(friendship);
    }

    @Test
    void removeUnknownFriendshipThrowsNotFound() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository, mock(UserNotificationRepository.class));

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));
        when(friendshipRepository.findForUser(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.remove("anna", 999L));
    }

    private FriendshipService createService(AppUserRepository userRepository, FriendshipRepository friendshipRepository, UserNotificationRepository notificationRepository) {
        return new FriendshipService(
                userRepository,
                friendshipRepository,
                notificationRepository,
                Clock.fixed(CURRENT_TIME, ZoneOffset.UTC)
        );
    }

    private AppUser user(Long id, String username, AvatarKey avatarKey) {
        AppUser user = mock(AppUser.class);

        when(user.getId()).thenReturn(id);
        when(user.getUsername()).thenReturn(username);
        when(user.getAvatarKey()).thenReturn(avatarKey);
        when(user.getAvatarColor()).thenReturn("#FFFFFF");

        return user;
    }

    private void stubResponseFields(Friendship friendship) {
        when(friendship.getId()).thenReturn(10L);
        when(friendship.getCreatedAt()).thenReturn(Instant.parse("2026-07-25T17:00:00Z"));
        when(friendship.getRespondedAt()).thenReturn(CURRENT_TIME);
    }
}