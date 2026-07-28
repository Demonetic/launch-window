package com.launchwindow.service.user;

import com.launchwindow.dto.friendship.CreateFriendRequest;
import com.launchwindow.dto.friendship.FriendshipResponse;
import com.launchwindow.exception.InvalidFriendshipException;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.model.*;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.FriendshipRepository;
import com.launchwindow.repository.UserNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

@Service
public class FriendshipService {
    private final AppUserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserNotificationRepository notificationRepository;
    private final Clock clock;

    public FriendshipService(AppUserRepository userRepository, FriendshipRepository friendshipRepository,
                             UserNotificationRepository notificationRepository, Clock clock) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    @Transactional
    public FriendshipResponse sendRequest(String username, CreateFriendRequest request) {
        AppUser requester = findUser(username);

        String identifier = request.identifier().trim();

        AppUser recipient = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found"));

        validateDifferentUsers(requester, recipient);

        long firstUserId = Math.min(requester.getId(), recipient.getId());

        long secondUserId = Math.max(requester.getId(), recipient.getId());

        friendshipRepository.findBetweenUsers(firstUserId, secondUserId).ifPresent(existing ->
                        handleExistingFriendship(existing));

        Friendship friendship = friendshipRepository.save(new Friendship(requester, recipient));

        notificationRepository.save(UserNotification.forFriendship(recipient, requester, NotificationType.FRIEND_REQUEST_RECEIVED, friendship));

        return map(friendship, requester.getId());
    }

    @Transactional(readOnly = true)
    public List<FriendshipResponse> getFriends(String username) {
        AppUser user = findUser(username);

        return friendshipRepository.findAllForUser(user.getId(), FriendshipStatus.ACCEPTED)
                .stream()
                .map(friendship -> map(friendship, user.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendshipResponse> getReceivedRequests(String username) {
        AppUser user = findUser(username);

        return friendshipRepository.findReceivedRequests(user.getId(), FriendshipStatus.PENDING)
                .stream()
                .map(friendship -> map(friendship, user.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendshipResponse> getSentRequests(String username) {
        AppUser user = findUser(username);

        return friendshipRepository.findSentRequests(user.getId(), FriendshipStatus.PENDING)
                .stream()
                .map(friendship -> map(friendship, user.getId()))
                .toList();
    }

    @Transactional
    public FriendshipResponse accept(String username, Long friendshipId) {
        AppUser user = findUser(username);

        Friendship friendship = findFriendship(friendshipId, user.getId());

        validateReceivedPendingRequest(friendship, user.getId());

        var respondedAt = clock.instant();

        friendship.accept(respondedAt);

        markRequestNotificationRead(user, friendship, respondedAt);

        notificationRepository.save(UserNotification.forFriendship(friendship.getRequester(), user, NotificationType.FRIEND_REQUEST_ACCEPTED, friendship));

        return map(friendship, user.getId());
    }

    @Transactional
    public FriendshipResponse decline(String username, Long friendshipId) {
        AppUser user = findUser(username);

        Friendship friendship = findFriendship(friendshipId, user.getId());

        validateReceivedPendingRequest(friendship, user.getId());

        var respondedAt = clock.instant();

        friendship.decline(respondedAt);

        markRequestNotificationRead(user, friendship, respondedAt);

        notificationRepository.save(UserNotification.forFriendship(friendship.getRequester(), user, NotificationType.FRIEND_REQUEST_DECLINED, friendship));

        return map(friendship, user.getId());
    }

    @Transactional
    public void remove(String username, Long friendshipId) {
        AppUser user = findUser(username);

        Friendship friendship = findFriendship(friendshipId, user.getId());

        detachFriendshipNotifications(friendship.getId());

        friendshipRepository.delete(friendship);
    }

    private AppUser findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
    }

    private Friendship findFriendship(Long friendshipId, Long userId) {
        return friendshipRepository
                .findForUser(friendshipId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship was not found"));
    }

    private void validateDifferentUsers(AppUser requester, AppUser recipient) {
        if (
                Objects.equals(requester.getId(), recipient.getId())) {
            throw new InvalidFriendshipException("You cannot send a friend request to yourself");
        }
    }

    private void handleExistingFriendship(Friendship friendship) {
        if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
            throw new InvalidFriendshipException("You are already friends");
        }

        if (friendship.getStatus() == FriendshipStatus.PENDING) {
            throw new InvalidFriendshipException("A friend request already exists");
        }

        detachFriendshipNotifications(friendship.getId());

        friendshipRepository.delete(friendship);
        friendshipRepository.flush();
    }

    private void validateReceivedPendingRequest(Friendship friendship, Long userId) {
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new InvalidFriendshipException("Friend request has already been answered");
        }

        if (friendship.wasRequestedBy(userId)) {
            throw new InvalidFriendshipException("You cannot answer your own friend request");
        }
    }

    private FriendshipResponse map(Friendship friendship, Long currentUserId) {
        AppUser otherUser = friendship.getOtherUser(currentUserId);

        return new FriendshipResponse(
                friendship.getId(),
                otherUser.getId(),
                otherUser.getUsername(),
                otherUser.getAvatarKey(),
                otherUser.getAvatarColor(),
                friendship.getStatus(),
                friendship.wasRequestedBy(currentUserId),
                friendship.getCreatedAt(),
                friendship.getRespondedAt()
        );
    }

    private void markRequestNotificationRead(AppUser recipient, Friendship friendship, java.time.Instant readAt) {
        notificationRepository.findByRecipient_IdAndFriendship_IdAndType(recipient.getId(), friendship.getId(), NotificationType.FRIEND_REQUEST_RECEIVED)
                .ifPresent(notification -> notification.resolveFriendship(friendship.getStatus(), readAt));
    }

    private void detachFriendshipNotifications(Long friendshipId) {
        List<UserNotification> notifications = notificationRepository.findAllByFriendship_Id(friendshipId);

        notifications.forEach(UserNotification::detachFriendship);

        notificationRepository.flush();
    }
}