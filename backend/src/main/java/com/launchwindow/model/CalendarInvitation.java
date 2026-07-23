package com.launchwindow.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "calendar_invitation",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_calendar_invitation_entry_invitee",
                columnNames = {
                        "calendar_entry_id",
                        "invitee_id"
                }
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalendarInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "calendar_entry_id", nullable = false)
    private CalendarEntry calendarEntry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inviter_id", nullable = false)
    private AppUser inviter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invitee_id", nullable = false)
    private AppUser invitee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CalendarInvitationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    public CalendarInvitation(CalendarEntry calendarEntry, AppUser inviter, AppUser invitee) {
        this.calendarEntry = calendarEntry;
        this.inviter = inviter;
        this.invitee = invitee;
        this.status = CalendarInvitationStatus.PENDING;
    }

    public void accept(Instant respondedAt) {
        this.status = CalendarInvitationStatus.ACCEPTED;
        this.respondedAt = respondedAt;
    }

    public void decline(Instant respondedAt) {
        this.status = CalendarInvitationStatus.DECLINED;
        this.respondedAt = respondedAt;
    }
}