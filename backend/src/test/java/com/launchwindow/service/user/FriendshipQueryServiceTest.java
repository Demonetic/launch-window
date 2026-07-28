package com.launchwindow.service.user;

import com.launchwindow.dto.friendship.FriendshipResponse;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.AvatarKey;
import com.launchwindow.model.Friendship;
import com.launchwindow.model.FriendshipStatus;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.FriendshipRepository;
import com.launchwindow.repository.UserNotificationRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FriendshipQueryServiceTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-25T18:00:00Z");

    @Test
    void getFriendsReturnsAcceptedFriends() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);
        AppUser alex = user(2L, "alex", AvatarKey.ALIEN);

        Friendship friendship = friendship(anna, alex, FriendshipStatus.ACCEPTED, false);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));
        when(friendshipRepository.findAllForUser(1L, FriendshipStatus.ACCEPTED)).thenReturn(List.of(friendship));

        List<FriendshipResponse> result = service.getFriends("anna");

        assertEquals(1, result.size());
        assertEquals(2L, result.getFirst().userId());
        assertEquals("alex", result.getFirst().username());
        assertEquals(FriendshipStatus.ACCEPTED, result.getFirst().status());
    }

    @Test
    void getReceivedRequestsReturnsIncomingRequests() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);
        AppUser alex = user(2L, "alex", AvatarKey.ALIEN);

        Friendship friendship = friendship(anna, alex, FriendshipStatus.PENDING, false);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));
        when(friendshipRepository.findReceivedRequests(1L, FriendshipStatus.PENDING)).thenReturn(List.of(friendship));

        List<FriendshipResponse> result = service.getReceivedRequests("anna");

        assertEquals(1, result.size());
        assertEquals("alex", result.getFirst().username());
        assertFalse(result.getFirst().requestedByCurrentUser());
    }

    @Test
    void getSentRequestsReturnsOutgoingRequests() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);
        AppUser alex = user(2L, "alex", AvatarKey.ALIEN);

        Friendship friendship = friendship(anna, alex, FriendshipStatus.PENDING, true);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));
        when(friendshipRepository.findSentRequests(1L, FriendshipStatus.PENDING)).thenReturn(List.of(friendship));

        List<FriendshipResponse> result = service.getSentRequests("anna");

        assertEquals(1, result.size());
        assertEquals("alex", result.getFirst().username());
        assertTrue(result.getFirst().requestedByCurrentUser());
    }

    private FriendshipService createService(AppUserRepository userRepository, FriendshipRepository friendshipRepository) {
        return new FriendshipService(
                userRepository,
                friendshipRepository,
                mock(UserNotificationRepository.class),
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

    private Friendship friendship(AppUser currentUser, AppUser otherUser, FriendshipStatus status, boolean requestedByCurrentUser) {
        Friendship friendship = mock(Friendship.class);

        when(friendship.getId()).thenReturn(10L);
        when(friendship.getOtherUser(currentUser.getId())).thenReturn(otherUser);
        when(friendship.wasRequestedBy(currentUser.getId())).thenReturn(requestedByCurrentUser);
        when(friendship.getStatus()).thenReturn(status);
        when(friendship.getCreatedAt()).thenReturn(Instant.parse("2026-07-25T17:00:00Z"));
        when(friendship.getRespondedAt()).thenReturn(CURRENT_TIME);

        return friendship;
    }
}