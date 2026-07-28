package com.launchwindow.repository;

import com.launchwindow.model.AppUser;
import com.launchwindow.model.Friendship;
import com.launchwindow.model.FriendshipStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.launchwindow.testsupport.AppUserTestData.user;
import static com.launchwindow.testsupport.FriendshipTestData.acceptedFriendship;
import static com.launchwindow.testsupport.FriendshipTestData.pendingFriendship;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class FriendshipRepositoryTest {
    private static final Instant RESPONSE_TIME = Instant.parse("2026-07-25T10:00:00Z");

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Test
    void findBetweenUsersReturnsFriendship() {
        AppUser anna = userRepository.save(user("anna", "anna@example.com"));
        AppUser alex = userRepository.save(user("alex", "alex@example.com"));

        Friendship friendship = friendshipRepository.saveAndFlush(pendingFriendship(anna, alex));

        Optional<Friendship> result =
                friendshipRepository.findBetweenUsers(Math.min(anna.getId(), alex.getId()), Math.max(anna.getId(), alex.getId()));

        assertTrue(result.isPresent());
        assertEquals(friendship.getId(), result.orElseThrow().getId());
    }

    @Test
    void findAllForUserReturnsAcceptedFriendsOnly() {
        AppUser anna = userRepository.save(user("anna", "anna@example.com"));
        AppUser alex = userRepository.save(user("alex", "alex@example.com"));
        AppUser sam = userRepository.save(user("sam", "sam@example.com"));

        Friendship accepted = acceptedFriendship(anna, alex, RESPONSE_TIME);
        Friendship pending = pendingFriendship(sam, anna);

        friendshipRepository.save(accepted);
        friendshipRepository.save(pending);
        friendshipRepository.flush();

        List<Friendship> result = friendshipRepository.findAllForUser(anna.getId(), FriendshipStatus.ACCEPTED);

        assertEquals(1, result.size());
        assertEquals(accepted.getId(), result.getFirst().getId());
        assertEquals(alex.getId(), result.getFirst().getOtherUser(anna.getId()).getId());
    }

    @Test
    void findReceivedRequestsExcludesSentRequests() {
        AppUser anna = userRepository.save(user("anna", "anna@example.com"));
        AppUser alex = userRepository.save(user("alex", "alex@example.com"));
        AppUser sam = userRepository.save(user("sam", "sam@example.com"));

        Friendship received = friendshipRepository.save(pendingFriendship(alex, anna));

        friendshipRepository.save(pendingFriendship(anna, sam)
        );

        friendshipRepository.flush();

        List<Friendship> result = friendshipRepository.findReceivedRequests(anna.getId(), FriendshipStatus.PENDING);

        assertEquals(1, result.size());
        assertEquals(received.getId(), result.getFirst().getId());
        assertEquals(alex.getId(), result.getFirst().getRequester().getId());
    }

    @Test
    void existsBetweenUsersWithStatusChecksAcceptedFriendship() {
        AppUser anna = userRepository.save(user("anna", "anna@example.com"));
        AppUser alex = userRepository.save(user("alex", "alex@example.com"));

        Friendship friendship = acceptedFriendship(anna, alex, RESPONSE_TIME);
        friendshipRepository.saveAndFlush(friendship);

        long firstUserId = Math.min(anna.getId(), alex.getId());
        long secondUserId = Math.max(anna.getId(), alex.getId());

        assertTrue(friendshipRepository.existsBetweenUsersWithStatus(firstUserId, secondUserId, FriendshipStatus.ACCEPTED));
        assertFalse(friendshipRepository.existsBetweenUsersWithStatus(firstUserId, secondUserId, FriendshipStatus.PENDING));
    }

    @Test
    void reverseFriendshipCannotBeInserted() {
        AppUser anna = userRepository.save(user("anna", "anna@example.com"));
        AppUser alex = userRepository.save(user("alex", "alex@example.com"));

        friendshipRepository.saveAndFlush(pendingFriendship(anna, alex));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> friendshipRepository.saveAndFlush(pendingFriendship(alex, anna))
        );
    }

    @Test
    void findForUserReturnsFriendshipOnlyForMember() {
        AppUser anna = userRepository.save(user("anna", "anna@example.com"));
        AppUser alex = userRepository.save(user("alex", "alex@example.com"));
        AppUser sam = userRepository.save(user("sam", "sam@example.com"));

        Friendship friendship = friendshipRepository.saveAndFlush(pendingFriendship(anna, alex));

        assertTrue(friendshipRepository.findForUser(friendship.getId(), anna.getId()).isPresent());
        assertTrue(friendshipRepository.findForUser(friendship.getId(), sam.getId()).isEmpty());
    }

    @Test
    void findSentRequestsReturnsOnlyRequestsCreatedByUser() {
        AppUser anna = userRepository.save(user("anna", "anna@example.com"));
        AppUser alex = userRepository.save(user("alex", "alex@example.com"));
        AppUser sam = userRepository.save(user("sam", "sam@example.com"));

        Friendship sent = friendshipRepository.save(pendingFriendship(anna, alex));

        friendshipRepository.save(pendingFriendship(sam, anna));

        friendshipRepository.flush();

        List<Friendship> result = friendshipRepository.findSentRequests(anna.getId(), FriendshipStatus.PENDING);

        assertEquals(1, result.size());
        assertEquals(sent.getId(), result.getFirst().getId());
    }

    @Test
    void countForUserWithStatusCountsAcceptedFriendsOnBothSides() {
        AppUser alex = userRepository.save(user("alex", "alex@example.com"));
        AppUser anna = userRepository.save(user("anna", "anna@example.com"));
        AppUser sam = userRepository.save(user("sam", "sam@example.com"));
        AppUser kim = userRepository.save(user("kim", "kim@example.com"));

        Friendship alexAndAnna = acceptedFriendship(alex, anna, RESPONSE_TIME);
        Friendship annaAndSam = acceptedFriendship(anna, sam, RESPONSE_TIME);
        Friendship annaAndKim = pendingFriendship(anna, kim);

        friendshipRepository.save(alexAndAnna);
        friendshipRepository.save(annaAndSam);
        friendshipRepository.save(annaAndKim);
        friendshipRepository.flush();

        long acceptedFriends = friendshipRepository.countForUserWithStatus(anna.getId(), FriendshipStatus.ACCEPTED);

        long pendingFriends = friendshipRepository.countForUserWithStatus(anna.getId(), FriendshipStatus.PENDING);

        assertEquals(2L, acceptedFriends);
        assertEquals(1L, pendingFriends);
    }

}
