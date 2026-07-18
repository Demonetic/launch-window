package com.launchwindow.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "weather_snapshot",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_weather_snapshot_launch_forecast",
                columnNames = {"launch_id", "forecast_time"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "launch_id", nullable = false)
    private Launch launch;

    @Column(name = "forecast_time", nullable = false)
    private Instant forecastTime;

    @Column(name = "temperature_c", nullable = false, precision = 5, scale = 2)
    private BigDecimal temperatureC;

    @Column(name = "cloud_cover_percent", nullable = false)
    private Short cloudCoverPercent;

    @Column(name = "precipitation_probability_percent", nullable = false)
    private Short precipitationProbabilityPercent;

    @Column(name = "wind_speed_kmh", nullable = false, precision = 6, scale = 2)
    private BigDecimal windSpeedKmh;

    @Column(name = "visibility_meters")
    private Integer visibilityMeters;

    @Column(name = "viewing_score", nullable = false)
    private Short viewingScore;

    @CreationTimestamp
    @Column(name = "fetched_at", nullable = false, updatable = false)
    private Instant fetchedAt;
}
