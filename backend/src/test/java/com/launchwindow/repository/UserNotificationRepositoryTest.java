package com.launchwindow.repository;

import com.launchwindow.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class UserNotificationRepositoryTest {
    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserNotificationRepository notificationRepository;

    @Test
    void findLatestForRecipientReturnsOnlyOwnNotifications() {
        AppUser anna = saveUser("anna", "anna@example.com");
        AppUser alex = saveUser("alex", "alex@example.com");
        AppUser sam = saveUser("sam", "sam@example.com");

        Friendship friendship = friendshipRepository.saveAndFlush(new Friendship(alex, anna));

        UserNotification first = notificationRepository.save(
                        UserNotification.forFriendship(anna, alex, NotificationType.FRIEND_REQUEST_RECEIVED, friendship));

        UserNotification second = notificationRepository.save(
                        UserNotification.forFriendship(anna, sam, NotificationType.FRIEND_REQUEST_ACCEPTED, friendship));

        notificationRepository.save(UserNotification.forFriendship(alex, anna, NotificationType.FRIEND_REQUEST_ACCEPTED, friendship));

        notificationRepository.flush();

        List<UserNotification> result =
                notificationRepository.findLatestForRecipient("anna", PageRequest.of(0, 20));

        assertEquals(2, result.size());
        assertEquals(List.of(second.getId(), first.getId()), result.stream()
                        .map(UserNotification::getId)
                        .toList()
        );
    }

    @Test
    void unreadCountChangesWhenNotificationIsRead() {
        AppUser anna = saveUser("anna", "anna@example.com");
        AppUser alex = saveUser("alex", "alex@example.com");

        Friendship friendship = friendshipRepository.saveAndFlush(new Friendship(alex, anna));

        UserNotification notification =
                notificationRepository.save(
                        UserNotification.forFriendship(anna, alex, NotificationType.FRIEND_REQUEST_RECEIVED, friendship));

        notificationRepository.flush();

        assertEquals(1, notificationRepository.countByRecipient_IdAndReadAtIsNull(anna.getId()));

        notification.markRead(Instant.parse("2026-07-27T09:00:00Z"));

        notificationRepository.flush();

        assertEquals(0, notificationRepository.countByRecipient_IdAndReadAtIsNull(anna.getId()));
    }

    @Test
    void findForRecipientDoesNotExposeAnotherUsersNotification() {
        AppUser anna = saveUser("anna", "anna@example.com");
        AppUser alex = saveUser("alex", "alex@example.com");
        Friendship friendship = friendshipRepository.saveAndFlush(new Friendship(alex, anna));

        UserNotification notification = notificationRepository.saveAndFlush(
                        UserNotification.forFriendship(anna, alex, NotificationType.FRIEND_REQUEST_RECEIVED, friendship));

        assertTrue(notificationRepository.findForRecipient(notification.getId(), "anna").isPresent());
        assertTrue(notificationRepository.findForRecipient(notification.getId(), "alex").isEmpty());
    }

    @Test
    void friendshipNotificationsRemainAfterFriendshipIsDeleted() {
        AppUser anna = saveUser("anna", "anna@example.com");
        AppUser alex = saveUser("alex", "alex@example.com");

        Friendship friendship = friendshipRepository.saveAndFlush(new Friendship(alex, anna));

        UserNotification receivedNotification = UserNotification.forFriendship(anna, alex, NotificationType.FRIEND_REQUEST_RECEIVED, friendship);

        Instant declinedAt = Instant.parse("2026-07-27T18:00:00Z");

        friendship.decline(declinedAt);

        receivedNotification.resolveFriendship(FriendshipStatus.DECLINED, declinedAt);

        UserNotification declinedNotification = UserNotification.forFriendship(alex, anna, NotificationType.FRIEND_REQUEST_DECLINED, friendship);

        notificationRepository.saveAllAndFlush(List.of(receivedNotification, declinedNotification));

        notificationRepository.findAllByFriendship_Id(friendship.getId()).forEach(UserNotification::detachFriendship);

        notificationRepository.flush();
        friendshipRepository.delete(friendship);
        friendshipRepository.flush();

        List<UserNotification> annaNotifications = notificationRepository.findLatestForRecipient("anna", PageRequest.of(0, 20));

        List<UserNotification> alexNotifications = notificationRepository.findLatestForRecipient("alex", PageRequest.of(0, 20));

        assertEquals(1, annaNotifications.size());
        assertEquals(1, alexNotifications.size());

        UserNotification receivedHistory = annaNotifications.getFirst();

        assertEquals(receivedNotification.getId(), receivedHistory.getId());
        assertNull(receivedHistory.getFriendship());
        assertEquals(FriendshipStatus.DECLINED, receivedHistory.getFriendshipStatus());
        assertTrue(receivedHistory.isRead());

        UserNotification declinedHistory = alexNotifications.getFirst();

        assertEquals(declinedNotification.getId(), declinedHistory.getId());
        assertNull(declinedHistory.getFriendship());
        assertEquals(FriendshipStatus.DECLINED, declinedHistory.getFriendshipStatus());
    }

    private AppUser saveUser(String username, String email) {
        return userRepository.save(new AppUser(username, email, "password-hash", Role.USER));
    }
}