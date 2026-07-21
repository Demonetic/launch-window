package com.launchwindow.repository;

import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchDetails;
import com.launchwindow.model.LaunchStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class LaunchWeatherRepositoryTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-22T12:00:00Z");

    @Autowired
    private LaunchRepository repository;

    @Test
    void weatherSyncQueryReturnsOnlyLaunchesWithCompleteCoordinatesInsidePeriod() {
        Launch eligible = saveLaunch(
                "eligible",
                CURRENT_TIME.plus(2, ChronoUnit.DAYS),
                new BigDecimal("28.500000"),
                new BigDecimal("-80.600000")
        );

        saveLaunch(
                "missing-latitude",
                CURRENT_TIME.plus(1, ChronoUnit.DAYS),
                null,
                new BigDecimal("-80.600000")
        );

        saveLaunch(
                "missing-longitude",
                CURRENT_TIME.plus(1, ChronoUnit.DAYS),
                new BigDecimal("28.500000"),
                null
        );

        saveLaunch(
                "before-period",
                CURRENT_TIME.minus(1, ChronoUnit.HOURS),
                new BigDecimal("28.500000"),
                new BigDecimal("-80.600000")
        );

        saveLaunch(
                "after-period",
                CURRENT_TIME.plus(17, ChronoUnit.DAYS),
                new BigDecimal("28.500000"),
                new BigDecimal("-80.600000")
        );

        List<Launch> result = repository
                .findAllByLaunchTimeBetweenAndLatitudeIsNotNullAndLongitudeIsNotNullOrderByLaunchTimeAsc(
                        CURRENT_TIME,
                        CURRENT_TIME.plus(16, ChronoUnit.DAYS)
                );

        assertEquals(List.of(eligible.getId()), result.stream()
                .map(Launch::getId)
                .toList());
    }

    private Launch saveLaunch(String externalId, Instant launchTime, BigDecimal latitude, BigDecimal longitude) {
        LaunchDetails details = new LaunchDetails(
                externalId,
                externalId,
                null,
                LaunchStatus.GO,
                launchTime,
                null,
                null,
                "Test rocket",
                null,
                null,
                null,
                null,
                latitude,
                longitude,
                CURRENT_TIME
        );

        return repository.save(new Launch(details));
    }
}