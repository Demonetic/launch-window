package com.launchwindow.repository;

import com.launchwindow.model.NotificationType;
import com.launchwindow.model.UserNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    @Query("""
            SELECT notification
            FROM UserNotification notification
            JOIN FETCH notification.recipient
            JOIN FETCH notification.actor
            LEFT JOIN FETCH notification.friendship
            LEFT JOIN FETCH notification.calendarInvitation
            WHERE notification.recipient.username = :username
            ORDER BY notification.createdAt DESC,
                     notification.id DESC
            """)
    List<UserNotification> findLatestForRecipient(@Param("username") String username, Pageable pageable);

    @Query("""
            SELECT notification
            FROM UserNotification notification
            JOIN FETCH notification.recipient
            JOIN FETCH notification.actor
            LEFT JOIN FETCH notification.friendship
            LEFT JOIN FETCH notification.calendarInvitation
            WHERE notification.id = :notificationId
              AND notification.recipient.username = :username
            """)
    Optional<UserNotification> findForRecipient(@Param("notificationId") Long notificationId, @Param("username") String username);

    Optional<UserNotification> findByRecipient_IdAndFriendship_IdAndType(Long recipientId, Long friendshipId, NotificationType type);

    Optional<UserNotification> findByRecipient_IdAndCalendarInvitation_IdAndType(Long recipientId, Long calendarInvitationId, NotificationType type);

    long countByRecipient_IdAndReadAtIsNull(Long recipientId);
}