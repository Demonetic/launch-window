package com.launchwindow.integration.launchlibrary;

import com.launchwindow.integration.launchlibrary.dto.*;
import com.launchwindow.model.LaunchDetails;
import com.launchwindow.model.LaunchStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
