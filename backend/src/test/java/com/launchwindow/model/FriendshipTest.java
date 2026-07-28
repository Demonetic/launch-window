package com.launchwindow.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.launchwindow.testsupport.AppUserTestData.persistedUser;
import static com.launchwindow.testsupport.FriendshipTestData.pendingFriendship;
import static org.junit.jupiter.api.Assertions.*;

class FriendshipTest {
    private static final Instant RESPONSE_TIME = Instant.parse("2026-07-25T10:00:00Z");

    @Test
    void newFriendshipOrdersUsersById() {
        AppUser requester = persistedUser(8L);
        AppUser recipient = persistedUser(3L);

        Friendship friendship = pendingFriendship(requester, recipient);

        assertEquals(recipient, friendship.getFirstUser());
        assertEquals(requester, friendship.getSecondUser());
        assertEquals(requester, friendship.getRequester());
        assertEquals(FriendshipStatus.PENDING, friendship.getStatus());
        assertNull(friendship.getRespondedAt());
    }

    @Test
    void acceptSetsAcceptedStatusAndResponseTime() {
        Friendship friendship = pendingFriendship(persistedUser(1L), persistedUser(2L));

        friendship.accept(RESPONSE_TIME);

        assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());
        assertEquals(RESPONSE_TIME, friendship.getRespondedAt());
    }

    @Test
    void declineSetsDeclinedStatusAndResponseTime() {
        Friendship friendship = pendingFriendship(persistedUser(1L), persistedUser(2L));

        friendship.decline(RESPONSE_TIME);

        assertEquals(FriendshipStatus.DECLINED, friendship.getStatus());
        assertEquals(RESPONSE_TIME, friendship.getRespondedAt());
    }

    @Test
    void getOtherUserReturnsFriend() {
        AppUser firstUser = persistedUser(1L);
        AppUser secondUser = persistedUser(2L);

        Friendship friendship = pendingFriendship(firstUser, secondUser);

        assertEquals(secondUser, friendship.getOtherUser(1L));
        assertEquals(firstUser, friendship.getOtherUser(2L));
    }

    @Test
    void getOtherUserRejectsUnrelatedUser() {
        Friendship friendship = pendingFriendship(persistedUser(1L), persistedUser(2L));

        assertThrows(IllegalArgumentException.class, () -> friendship.getOtherUser(3L));
    }

    @Test
    void creatingFriendshipWithSameUserIsRejected() {
        AppUser user = persistedUser(1L);

        assertThrows(IllegalArgumentException.class, () -> new Friendship(user, user));
    }

    @Test
    void creatingFriendshipWithUnpersistedUserIsRejected() {
        AppUser persistedUser = persistedUser(1L);

        AppUser unpersistedUser = new AppUser("alex", "alex@example.com", "password-hash", Role.USER);

        assertNull(unpersistedUser.getId());
        assertThrows(IllegalArgumentException.class, () -> new Friendship(persistedUser, unpersistedUser));
    }

}
