package com.launchwindow.integration.launchlibrary;

import com.launchwindow.integration.launchlibrary.dto.*;
import com.launchwindow.model.LaunchDetails;
import com.launchwindow.model.LaunchStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LaunchMapperTest {
    private final LaunchMapper mapper = new LaunchMapper(new LaunchStatusMapper());

    @Test
    void mapsLaunchLibraryDataToLaunchDetails() {
        Instant launchTime = Instant.parse("2026-08-01T10:15:30Z");
        Instant syncedAt = Instant.parse("2026-07-18T14:00:00Z");

        LaunchLibraryLaunchDto source = new LaunchLibraryLaunchDto(
                "launch-123",
                "Artemis Test Launch",
                new LaunchLibraryStatusDto(1, "Go", "Go"),
                launchTime,
                new LaunchLibraryImageDto("https://example.com/image.jpg"),
                new LaunchLibraryRocketDto(
                        new LaunchLibraryRocketConfigurationDto("SLS Block 1")
                ),
                new LaunchLibraryMissionDto(
                        "Exploration",
                        "Test mission description"
                ),
                new LaunchLibraryPadDto(
                        "Launch Complex 39B",
                        new BigDecimal("28.627000"),
                        new BigDecimal("-80.621000"),
                        new LaunchLibraryLocationDto("Kennedy Space Center")
                ),
                new LaunchLibraryAgencyDto("NASA"),
                List.of(
                        new LaunchLibraryVideoDto(
                                "https://example.com/webcast"
                        )
                )
        );

        LaunchDetails result = mapper.map(source, syncedAt);

        assertAll(
                () -> assertEquals("launch-123", result.externalId()),
                () -> assertEquals("Artemis Test Launch", result.name()),
                () -> assertEquals(
                        "Test mission description",
                        result.description()
                ),
                () -> assertEquals(LaunchStatus.GO, result.status()),
                () -> assertEquals(launchTime, result.launchTime()),
                () -> assertEquals(
                        "https://example.com/image.jpg",
                        result.imageUrl()
                ),
                () -> assertEquals(
                        "https://example.com/webcast",
                        result.webcastUrl()
                ),
                () -> assertEquals("SLS Block 1", result.rocketName()),
                () -> assertEquals("Exploration", result.missionType()),
                () -> assertEquals("NASA", result.organizationName()),
                () -> assertEquals("Launch Complex 39B", result.padName()),
                () -> assertEquals(
                        "Kennedy Space Center",
                        result.locationName()
                ),
                () -> assertEquals(
                        new BigDecimal("28.627000"),
                        result.latitude()
                ),
                () -> assertEquals(
                        new BigDecimal("-80.621000"),
                        result.longitude()
                ),
                () -> assertEquals(syncedAt, result.lastSyncedAt())
        );
    }

    @Test
    void mapsMissingOptionalDataToSafeFallbacks() {
        Instant launchTime = Instant.parse("2026-08-01T10:15:30Z");
        Instant syncedAt = Instant.parse("2026-07-22T12:00:00Z");

        LaunchLibraryLaunchDto source = new LaunchLibraryLaunchDto(
                "launch-123",
                "Test launch",
                null,
                launchTime,
                null,
                null,
                null,
                null,
                null,
                null
        );

        LaunchDetails result = mapper.map(source, syncedAt);

        assertAll(
                () -> assertEquals(LaunchStatus.UNKNOWN, result.status()),
                () -> assertEquals("Unknown rocket", result.rocketName()),
                () -> assertNull(result.description()),
                () -> assertNull(result.imageUrl()),
                () -> assertNull(result.webcastUrl()),
                () -> assertNull(result.missionType()),
                () -> assertNull(result.organizationName()),
                () -> assertNull(result.padName()),
                () -> assertNull(result.locationName()),
                () -> assertNull(result.latitude()),
                () -> assertNull(result.longitude())
        );
    }

    @Test
    void skipsMissingAndBlankVideoUrls() {
        LaunchLibraryLaunchDto source = new LaunchLibraryLaunchDto(
                "launch-123",
                "Test launch",
                new LaunchLibraryStatusDto(1, "Go", "Go"),
                Instant.parse("2026-08-01T10:15:30Z"),
                null,
                null,
                null,
                null,
                null,
                List.of(new LaunchLibraryVideoDto(" "), new LaunchLibraryVideoDto("https://example.com/webcast"))
        );

        LaunchDetails result = mapper.map(source, Instant.parse("2026-07-22T12:00:00Z"));

        assertEquals("https://example.com/webcast", result.webcastUrl());
    }

    @Test
    void mapsOutOfRangeCoordinatesToNull() {
        LaunchLibraryLaunchDto source = launchWithCoordinates(
                new BigDecimal("90.000001"),
                new BigDecimal("-180.000001")
        );

        LaunchDetails result = mapper.map(
                source,
                Instant.parse("2026-07-22T12:00:00Z")
        );

        assertAll(
                () -> assertNull(result.latitude()),
                () -> assertNull(result.longitude())
        );
    }

    @Test
    void preservesCoordinateBoundaryValues() {
        LaunchLibraryLaunchDto source = launchWithCoordinates(new BigDecimal("-90"), new BigDecimal("180"));

        LaunchDetails result = mapper.map(source, Instant.parse("2026-07-22T12:00:00Z"));

        assertAll(
                () -> assertEquals(new BigDecimal("-90"), result.latitude()),
                () -> assertEquals(new BigDecimal("180"), result.longitude())
        );
    }

    private LaunchLibraryLaunchDto launchWithCoordinates(BigDecimal latitude, BigDecimal longitude) {
        return new LaunchLibraryLaunchDto(
                "launch-123",
                "Test launch",
                new LaunchLibraryStatusDto(1, "Go", "Go"),
                Instant.parse("2026-08-01T10:15:30Z"),
                null,
                null,
                null,
                new LaunchLibraryPadDto("Test pad", latitude, longitude, new LaunchLibraryLocationDto("Test location")),
                null,
                null
        );
    }
}
