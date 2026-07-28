package com.launchwindow.service.notification;

import com.launchwindow.dto.notification.NotificationResponse;
import com.launchwindow.dto.notification.UnreadNotificationCountResponse;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.CalendarInvitation;
import com.launchwindow.model.Friendship;
import com.launchwindow.model.Launch;
import com.launchwindow.model.UserNotification;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.UserNotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
public class NotificationService {
    private static final int NOTIFICATION_LIMIT = 50;

    private final AppUserRepository userRepository;
    private final UserNotificationRepository notificationRepository;
    private final Clock clock;

    public NotificationService(AppUserRepository userRepository, UserNotificationRepository notificationRepository, Clock clock) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getLatest(String username) {
        return notificationRepository.findLatestForRecipient(username, PageRequest.of(0, NOTIFICATION_LIMIT))
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse getUnreadCount(String username) {
        AppUser user = findUser(username);

        long unreadCount = notificationRepository.countByRecipient_IdAndReadAtIsNull(user.getId());

        return new UnreadNotificationCountResponse(unreadCount);
    }

    @Transactional
    public void markRead(String username, Long notificationId) {
        UserNotification notification = notificationRepository.findForRecipient(notificationId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Notification was not found"));

        notification.markRead(clock.instant());
    }

    @Transactional
    public void markAllRead(String username) {
        AppUser user = findUser(username);

        var readAt = clock.instant();

        notificationRepository.findAllByRecipient_IdAndReadAtIsNull(user.getId())
                .forEach(notification -> notification.markRead(readAt));
    }

    private AppUser findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
    }

    private NotificationResponse map(UserNotification notification) {
        AppUser actor = notification.getActor();
        Friendship friendship = notification.getFriendship();
        CalendarInvitation invitation = notification.getCalendarInvitation();

        Launch launch = invitation == null
                ? null
                : invitation.getCalendarEntry().getLaunch();

        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt(),
                actor.getId(),
                actor.getUsername(),
                actor.getAvatarKey(),
                actor.getAvatarColor(),
                friendship == null ? null : friendship.getId(),
                notification.getFriendshipStatus(),
                invitation == null ? null : invitation.getId(),
                invitation == null ? null : invitation.getStatus(),
                launch == null ? null : launch.getId(),
                launch == null ? null : launch.getName(),
                launch == null ? null : launch.getLaunchTime()
        );
    }
}