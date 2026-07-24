package com.launchwindow.repository;

import com.launchwindow.model.CalendarInvitationStatus;
import com.launchwindow.model.LaunchNote;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LaunchNoteRepository extends JpaRepository<LaunchNote, Long> {
    Optional<LaunchNote> findByIdAndUser_Id(Long noteId, Long userId);

    @Query("""
            SELECT note
            FROM LaunchNote note
            JOIN FETCH note.launch
            WHERE note.user.id = :userId
            ORDER BY note.updatedAt DESC, note.id DESC
            """)
    List<LaunchNote> findOverviewInitial(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
            SELECT note
            FROM LaunchNote note
            JOIN FETCH note.launch
            WHERE note.user.id = :userId
              AND (
                    note.updatedAt < :beforeUpdatedAt
                    OR (
                        note.updatedAt = :beforeUpdatedAt
                        AND note.id < :beforeId
                    )
                  )
            ORDER BY note.updatedAt DESC, note.id DESC
            """)
    List<LaunchNote> findOverviewPage(@Param("userId") Long userId, @Param("beforeUpdatedAt") Instant beforeUpdatedAt,
                                      @Param("beforeId") Long beforeId, Pageable pageable);

    @Query("""
        SELECT DISTINCT note
        FROM LaunchNote note
        JOIN FETCH note.user author
        WHERE note.launch.id = :launchId
          AND (
                author.id = :viewerId
                OR EXISTS (
                    SELECT invitation.id
                    FROM CalendarInvitation invitation
                    WHERE invitation.status = :status
                      AND invitation.calendarEntry.launch.id = :launchId
                      AND (
                            invitation.inviter.id = author.id
                            OR invitation.invitee.id = author.id
                          )
                      AND invitation.calendarEntry.id IN (
                            SELECT membership.calendarEntry.id
                            FROM CalendarInvitation membership
                            WHERE membership.status = :status
                              AND (
                                    membership.inviter.id = :viewerId
                                    OR membership.invitee.id = :viewerId
                                  )
                          )
                )
              )
        ORDER BY note.createdAt DESC, note.id DESC
        """)
    List<LaunchNote> findAccessibleByLaunchId(@Param("viewerId") Long viewerId, @Param("launchId") Long launchId,
                                              @Param("status") CalendarInvitationStatus status);
}