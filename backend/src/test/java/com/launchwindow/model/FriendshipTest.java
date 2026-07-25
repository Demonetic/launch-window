package com.launchwindow.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FriendshipTest {
    private static final Instant RESPONSE_TIME = Instant.parse("2026-07-25T10:00:00Z");

    @Test
    void newFriendshipOrdersUsersById() {
        AppUser requester = user(8L);
        AppUser recipient = user(3L);

        Friendship friendship = new Friendship(requester, recipient);

        assertEquals(recipient, friendship.getFirstUser());
        assertEquals(requester, friendship.getSecondUser());
        assertEquals(requester, friendship.getRequester());
        assertEquals(FriendshipStatus.PENDING, friendship.getStatus());
        assertNull(friendship.getRespondedAt());
    }

    @Test
    void acceptSetsAcceptedStatusAndResponseTime() {
        Friendship friendship = new Friendship(user(1L), user(2L));

        friendship.accept(RESPONSE_TIME);

        assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());
        assertEquals(RESPONSE_TIME, friendship.getRespondedAt());
    }

    @Test
    void declineSetsDeclinedStatusAndResponseTime() {
        Friendship friendship = new Friendship(user(1L), user(2L));

        friendship.decline(RESPONSE_TIME);

        assertEquals(FriendshipStatus.DECLINED, friendship.getStatus());
        assertEquals(RESPONSE_TIME, friendship.getRespondedAt());
    }

    @Test
    void getOtherUserReturnsFriend() {
        AppUser firstUser = user(1L);
        AppUser secondUser = user(2L);

        Friendship friendship = new Friendship(firstUser, secondUser);

        assertEquals(secondUser, friendship.getOtherUser(1L));
        assertEquals(firstUser, friendship.getOtherUser(2L));
    }

    @Test
    void getOtherUserRejectsUnrelatedUser() {
        Friendship friendship = new Friendship(user(1L), user(2L));

        assertThrows(IllegalArgumentException.class, () -> friendship.getOtherUser(3L));
    }

    @Test
    void creatingFriendshipWithSameUserIsRejected() {
        AppUser user = user(1L);

        assertThrows(IllegalArgumentException.class, () -> new Friendship(user, user));
    }

    @Test
    void creatingFriendshipWithUnpersistedUserIsRejected() {
        AppUser persistedUser = user(1L);

        AppUser unpersistedUser = new AppUser("alex", "alex@example.com", "password-hash", Role.USER);

        assertNull(unpersistedUser.getId());
        assertThrows(IllegalArgumentException.class, () -> new Friendship(persistedUser, unpersistedUser));
    }

    private AppUser user(Long id) {
        AppUser user = mock(AppUser.class);
        when(user.getId()).thenReturn(id);
        return user;
    }
}