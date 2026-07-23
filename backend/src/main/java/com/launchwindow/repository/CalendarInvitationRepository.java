package com.launchwindow.repository;

import com.launchwindow.model.CalendarInvitation;
import com.launchwindow.model.CalendarInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CalendarInvitationRepository extends JpaRepository<CalendarInvitation, Long> {
    boolean existsByCalendarEntry_IdAndInvitee_Id(Long calendarEntryId, Long inviteeId);

    @Query("""
            SELECT invitation
            FROM CalendarInvitation invitation
            JOIN FETCH invitation.calendarEntry entry
            JOIN FETCH entry.launch
            JOIN FETCH invitation.inviter
            WHERE invitation.id = :invitationId
              AND invitation.invitee.username = :username
            """)
    Optional<CalendarInvitation> findForInvitee(@Param("invitationId") Long invitationId, @Param("username") String username);

    @Query("""
            SELECT invitation
            FROM CalendarInvitation invitation
            JOIN FETCH invitation.calendarEntry entry
            JOIN FETCH entry.launch
            JOIN FETCH invitation.inviter
            WHERE invitation.invitee.username = :username
              AND invitation.status = :status
            ORDER BY invitation.createdAt DESC,
                     invitation.id DESC
            """)
    List<CalendarInvitation> findAllForInvitee(@Param("username") String username, @Param("status") CalendarInvitationStatus status);
}