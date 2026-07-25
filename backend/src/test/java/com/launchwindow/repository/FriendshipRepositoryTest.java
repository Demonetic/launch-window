package com.launchwindow.repository;

import com.launchwindow.model.AppUser;
import com.launchwindow.model.Friendship;
import com.launchwindow.model.FriendshipStatus;
import com.launchwindow.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
        AppUser anna = saveUser("anna", "anna@example.com");
        AppUser alex = saveUser("alex", "alex@example.com");

        Friendship friendship = friendshipRepository.saveAndFlush(new Friendship(anna, alex));

        Optional<Friendship> result =
                friendshipRepository.findBetweenUsers(Math.min(anna.getId(), alex.getId()), Math.max(anna.getId(), alex.getId()));

        assertTrue(result.isPresent());
        assertEquals(friendship.getId(), result.orElseThrow().getId());
    }

    @Test
    void findAllForUserReturnsAcceptedFriendsOnly() {
        AppUser anna = saveUser("anna", "anna@example.com");
        AppUser alex = saveUser("alex", "alex@example.com");
        AppUser sam = saveUser("sam", "sam@example.com");

        Friendship accepted = new Friendship(anna, alex);
        accepted.accept(RESPONSE_TIME);

        Friendship pending = new Friendship(sam, anna);

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
        AppUser anna = saveUser("anna", "anna@example.com");
        AppUser alex = saveUser("alex", "alex@example.com");
        AppUser sam = saveUser("sam", "sam@example.com");

        Friendship received = friendshipRepository.save(new Friendship(alex, anna));

        friendshipRepository.save(new Friendship(anna, sam)
        );

        friendshipRepository.flush();

        List<Friendship> result = friendshipRepository.findReceivedRequests(anna.getId(), FriendshipStatus.PENDING);

        assertEquals(1, result.size());
        assertEquals(received.getId(), result.getFirst().getId());
        assertEquals(alex.getId(), result.getFirst().getRequester().getId());
    }

    @Test
    void existsBetweenUsersWithStatusChecksAcceptedFriendship() {
        AppUser anna = saveUser("anna", "anna@example.com");
        AppUser alex = saveUser("alex", "alex@example.com");

        Friendship friendship = new Friendship(anna, alex);

        friendship.accept(RESPONSE_TIME);
        friendshipRepository.saveAndFlush(friendship);

        long firstUserId = Math.min(anna.getId(), alex.getId());
        long secondUserId = Math.max(anna.getId(), alex.getId());

        assertTrue(friendshipRepository.existsBetweenUsersWithStatus(firstUserId, secondUserId, FriendshipStatus.ACCEPTED));
        assertFalse(friendshipRepository.existsBetweenUsersWithStatus(firstUserId, secondUserId, FriendshipStatus.PENDING));
    }

    @Test
    void reverseFriendshipCannotBeInserted() {
        AppUser anna = saveUser("anna", "anna@example.com");
        AppUser alex = saveUser("alex", "alex@example.com");

        friendshipRepository.saveAndFlush(new Friendship(anna, alex));

        assertThrows(
                DataIntegrityViolationException.class, () -> friendshipRepository.saveAndFlush(new Friendship(alex, anna))
        );
    }

    private AppUser saveUser(String username, String email) {
        return userRepository.save(
                new AppUser(
                        username,
                        email,
                        "password-hash",
                        Role.USER
                )
        );
    }
}