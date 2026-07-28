package com.launchwindow.service.user;

import com.launchwindow.dto.friendship.CreateFriendRequest;
import com.launchwindow.dto.friendship.FriendshipResponse;
import com.launchwindow.exception.InvalidFriendshipException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FriendshipRequestServiceTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-25T18:00:00Z");

    @Test
    void sendRequestCreatesPendingFriendship() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);
        UserNotificationRepository notificationRepository = mock(UserNotificationRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository, notificationRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);
        AppUser alex = user(2L, "alex", AvatarKey.ALIEN);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));
        when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("alex", "alex")).thenReturn(Optional.of(alex));
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FriendshipResponse result = service.sendRequest("anna", new CreateFriendRequest("  alex  "));

        assertEquals(2L, result.userId());
        assertEquals("alex", result.username());
        assertEquals(FriendshipStatus.PENDING, result.status());
        assertTrue(result.requestedByCurrentUser());

        ArgumentCaptor<Friendship> friendshipCaptor = ArgumentCaptor.forClass(Friendship.class);

        verify(friendshipRepository).save(friendshipCaptor.capture());

        Friendship friendship = friendshipCaptor.getValue();

        assertEquals(anna, friendship.getRequester());
        assertEquals(anna, friendship.getFirstUser());
        assertEquals(alex, friendship.getSecondUser());
    }

    @Test
    void sendRequestCreatesNotificationForRecipient() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);
        UserNotificationRepository notificationRepository = mock(UserNotificationRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository, notificationRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);
        AppUser alex = user(2L, "alex", AvatarKey.ALIEN);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));
        when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("alex", "alex")).thenReturn(Optional.of(alex));
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.sendRequest("anna", new CreateFriendRequest("alex"));

        ArgumentCaptor<UserNotification> notificationCaptor = ArgumentCaptor.forClass(UserNotification.class);

        verify(notificationRepository).save(notificationCaptor.capture());

        UserNotification notification = notificationCaptor.getValue();

        assertEquals(alex, notification.getRecipient());
        assertEquals(anna, notification.getActor());
        assertEquals(NotificationType.FRIEND_REQUEST_RECEIVED, notification.getType());
        assertNotNull(notification.getFriendship());
    }

    @Test
    void sendRequestRejectsSelf() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);
        UserNotificationRepository notificationRepository = mock(UserNotificationRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository, notificationRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));
        when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("anna", "anna")).thenReturn(Optional.of(anna));

        InvalidFriendshipException exception = assertThrows(
                InvalidFriendshipException.class, () -> service.sendRequest("anna", new CreateFriendRequest("anna"))
        );

        assertEquals("You cannot send a friend request to yourself", exception.getMessage());

        verify(friendshipRepository, never()).save(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendRequestRejectsExistingPendingRequest() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);
        UserNotificationRepository notificationRepository = mock(UserNotificationRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository, notificationRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);
        AppUser alex = user(2L, "alex", AvatarKey.ALIEN);
        Friendship existing = mock(Friendship.class);

        when(existing.getStatus()).thenReturn(FriendshipStatus.PENDING);

        stubUsers(userRepository, anna, alex);

        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(existing));

        InvalidFriendshipException exception = assertThrows(
                InvalidFriendshipException.class, () -> service.sendRequest("anna", new CreateFriendRequest("alex")));

        assertEquals("A friend request already exists", exception.getMessage());

        verify(friendshipRepository, never()).save(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendRequestReplacesDeclinedRequestWithoutDeletingHistory() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);
        UserNotificationRepository notificationRepository = mock(UserNotificationRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository, notificationRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);
        AppUser alex = user(2L, "alex", AvatarKey.ALIEN);

        Friendship declined = mock(Friendship.class);

        UserNotification oldReceivedNotification = mock(UserNotification.class);
        UserNotification oldDeclinedNotification = mock(UserNotification.class);

        when(declined.getId()).thenReturn(10L);
        when(declined.getStatus()).thenReturn(FriendshipStatus.DECLINED);

        stubUsers(userRepository, anna, alex);

        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(declined));

        when(notificationRepository.findAllByFriendship_Id(10L)).thenReturn(List.of(oldReceivedNotification, oldDeclinedNotification));

        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FriendshipResponse result = service.sendRequest("anna", new CreateFriendRequest("alex"));

        assertEquals(FriendshipStatus.PENDING, result.status());

        verify(oldReceivedNotification).detachFriendship();
        verify(oldDeclinedNotification).detachFriendship();
        verify(notificationRepository).flush();
        verify(friendshipRepository).delete(declined);
        verify(friendshipRepository).flush();
        verify(friendshipRepository).save(any(Friendship.class));
        verify(notificationRepository).save(any(UserNotification.class));
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

    private void stubUsers(AppUserRepository repository, AppUser requester, AppUser recipient) {
        when(repository.findByUsername("anna")).thenReturn(Optional.of(requester));
        when(repository.findByUsernameIgnoreCaseOrEmailIgnoreCase("alex", "alex")).thenReturn(Optional.of(recipient));
    }
}