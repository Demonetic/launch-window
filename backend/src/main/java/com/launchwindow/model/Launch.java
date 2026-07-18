package com.launchwindow.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "launch")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Launch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, length = 100)
    private String externalId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LaunchStatus status;

    @Column(name = "launch_time", nullable = false)
    private Instant launchTime;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "webcast_url", columnDefinition = "TEXT")
    private String webcastUrl;

    @Column(name = "rocket_name", nullable = false, length = 255)
    private String rocketName;

    @Column(name = "mission_type", length = 100)
    private String missionType;

    @Column(name = "organization_name", length = 255)
    private String organizationName;

    @Column(name = "pad_name", length = 255)
    private String padName;

    @Column(name = "location_name", length = 255)
    private String locationName;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "last_synced_at", nullable = false)
    private Instant lastSyncedAt;

    public Launch(LaunchDetails details) {
        updateFrom(details);
    }

    public void updateFrom(LaunchDetails details) {
        this.externalId = details.externalId();
        this.name = details.name();
        this.description = details.description();
        this.status = details.status();
        this.launchTime = details.launchTime();
        this.imageUrl = details.imageUrl();
        this.webcastUrl = details.webcastUrl();
        this.rocketName = details.rocketName();
        this.missionType = details.missionType();
        this.organizationName = details.organizationName();
        this.padName = details.padName();
        this.locationName = details.locationName();
        this.latitude = details.latitude();
        this.longitude = details.longitude();
        this.lastSyncedAt = details.lastSyncedAt();
    }
}
