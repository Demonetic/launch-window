package com.launchwindow.testsupport;

import com.launchwindow.model.AppUser;
import com.launchwindow.model.Friendship;

import java.time.Instant;

public final class FriendshipTestData {
    private FriendshipTestData() {
    }

    public static Friendship pendingFriendship(AppUser requester, AppUser recipient) {
        return new Friendship(requester, recipient);
    }

    public static Friendship acceptedFriendship(AppUser requester, AppUser recipient, Instant respondedAt) {
        Friendship friendship = pendingFriendship(requester, recipient);

        friendship.accept(respondedAt);

        return friendship;
    }
}
