package com.launchwindow.service.user;

import com.launchwindow.dto.CreateFriendRequest;
import com.launchwindow.dto.FriendshipResponse;
import com.launchwindow.exception.InvalidFriendshipException;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.model.*;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.FriendshipRepository;
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

class FriendshipServiceTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-25T18:00:00Z");

    @Test
    void sendRequestCreatesPendingFriendship() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository);

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

        ArgumentCaptor<Friendship> captor = ArgumentCaptor.forClass(Friendship.class);

        verify(friendshipRepository).save(captor.capture());

        Friendship friendship = captor.getValue();

        assertEquals(anna, friendship.getRequester());
        assertEquals(anna, friendship.getFirstUser());
        assertEquals(alex, friendship.getSecondUser());
    }

    @Test
    void sendRequestRejectsSelf() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));

        when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("anna", "anna")).thenReturn(Optional.of(anna));

        InvalidFriendshipException exception = assertThrows(
                        InvalidFriendshipException.class, () -> service.sendRequest("anna", new CreateFriendRequest("anna")));

        assertEquals("You cannot send a friend request to yourself", exception.getMessage());

        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendRequestRejectsExistingPendingRequest() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);

        AppUser alex = user(2L, "alex", AvatarKey.ALIEN);

        Friendship existing = mock(Friendship.class);

        when(existing.getStatus()).thenReturn(FriendshipStatus.PENDING);

        stubUsers(userRepository, anna, alex);

        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(existing));

        InvalidFriendshipException exception = assertThrows(
                        InvalidFriendshipException.class, () -> service.sendRequest("anna", new CreateFriendRequest("alex"))
                );

        assertEquals("A friend request already exists", exception.getMessage());

        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendRequestReplacesDeclinedRequest() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);

        AppUser alex = user(2L, "alex", AvatarKey.ALIEN);

        Friendship declined = mock(Friendship.class);

        when(declined.getStatus()).thenReturn(FriendshipStatus.DECLINED);

        stubUsers(userRepository, anna, alex);

        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(declined));
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FriendshipResponse result = service.sendRequest("anna", new CreateFriendRequest("alex"));

        assertEquals(FriendshipStatus.PENDING, result.status());

        verify(friendshipRepository).delete(declined);
        verify(friendshipRepository).flush();
        verify(friendshipRepository).save(any(Friendship.class));
    }

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
        assertTrue(result.getFirst().requestedByCurrentUser());
    }

    @Test
    void acceptAcceptsReceivedPendingRequest() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);

        AppUser alex = user(2L, "alex", AvatarKey.ALIEN);

        Friendship friendship = mock(Friendship.class);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));
        when(friendshipRepository.findForUser(10L, 1L)).thenReturn(Optional.of(friendship));
        when(friendship.getStatus()).thenReturn(FriendshipStatus.PENDING, FriendshipStatus.ACCEPTED);
        when(friendship.wasRequestedBy(1L)).thenReturn(false);
        when(friendship.getOtherUser(1L)).thenReturn(alex);

        stubResponseFields(friendship);

        FriendshipResponse result = service.accept("anna", 10L);

        verify(friendship).accept(CURRENT_TIME);

        assertEquals(FriendshipStatus.ACCEPTED, result.status());
        assertEquals(CURRENT_TIME, result.respondedAt());
    }

    @Test
    void declineDeclinesReceivedPendingRequest() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);
        AppUser alex = user(2L, "alex", AvatarKey.ALIEN);

        Friendship friendship = mock(Friendship.class);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));
        when(friendshipRepository.findForUser(10L, 1L)).thenReturn(Optional.of(friendship));
        when(friendship.getStatus()).thenReturn(FriendshipStatus.PENDING, FriendshipStatus.DECLINED);
        when(friendship.wasRequestedBy(1L)).thenReturn(false);
        when(friendship.getOtherUser(1L)).thenReturn(alex);

        stubResponseFields(friendship);

        FriendshipResponse result = service.decline("anna", 10L);

        verify(friendship).decline(CURRENT_TIME);

        assertEquals(FriendshipStatus.DECLINED, result.status());
    }

    @Test
    void acceptRejectsOwnOutgoingRequest() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);
        Friendship friendship = mock(Friendship.class);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));
        when(friendshipRepository.findForUser(10L, 1L)).thenReturn(Optional.of(friendship));
        when(friendship.getStatus()).thenReturn(FriendshipStatus.PENDING);
        when(friendship.wasRequestedBy(1L)).thenReturn(true);

        InvalidFriendshipException exception = assertThrows(
                InvalidFriendshipException.class, () -> service.accept("anna", 10L));

        assertEquals("You cannot answer your own friend request", exception.getMessage());

        verify(friendship, never()).accept(any());
    }

    @Test
    void removeDeletesFriendshipForMember() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        FriendshipService service = createService(userRepository, friendshipRepository);

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

        FriendshipService service = createService(userRepository, friendshipRepository);

        AppUser anna = user(1L, "anna", AvatarKey.ASTRONAUT);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(anna));

        when(friendshipRepository.findForUser(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.remove("anna", 999L));
    }

    private FriendshipService createService(AppUserRepository userRepository, FriendshipRepository friendshipRepository) {
        return new FriendshipService(
                userRepository,
                friendshipRepository,
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

    private Friendship friendship(AppUser currentUser, AppUser otherUser, FriendshipStatus status, boolean requestedByCurrentUser) {
        Friendship friendship = mock(Friendship.class);

        when(friendship.getOtherUser(currentUser.getId())).thenReturn(otherUser);
        when(friendship.wasRequestedBy(currentUser.getId())).thenReturn(requestedByCurrentUser);
        when(friendship.getStatus()).thenReturn(status);

        stubResponseFields(friendship);

        return friendship;
    }

    private void stubResponseFields(Friendship friendship) {
        when(friendship.getId()).thenReturn(10L);
        when(friendship.getCreatedAt()).thenReturn(Instant.parse("2026-07-25T17:00:00Z"));
        when(friendship.getRespondedAt()).thenReturn(CURRENT_TIME);
    }
}