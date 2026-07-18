package com.launchwindow.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "calendar_entry",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_calendar_entry_user_launch",
                columnNames = {"user_id", "launch_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalendarEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "launch_id", nullable = false)
    private Launch launch;

    @CreationTimestamp
    @Column(name = "saved_at", nullable = false, updatable = false)
    private Instant savedAt;

    public CalendarEntry(AppUser user, Launch launch) {
        this.user = user;
        this.launch = launch;
    }
}
