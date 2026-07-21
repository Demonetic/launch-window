package com.launchwindow.repository;

import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchDetails;
import com.launchwindow.model.LaunchStatus;
import com.launchwindow.model.WeatherDetails;
import com.launchwindow.model.WeatherSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class BestViewingRepositoryTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-21T12:00:00Z");

    @Autowired
    private LaunchRepository launchRepository;

    @Autowired
    private WeatherSnapshotRepository weatherRepository;

    private Launch bestLaunch;
    private Launch firstEqualLaunch;
    private Launch secondEqualLaunch;
    private Launch laterEqualLaunch;
    private Launch lowerScoreLaunch;

    @BeforeEach
    void setUp() {
        bestLaunch = saveLaunch("best", "Best launch", CURRENT_TIME.plus(3, ChronoUnit.DAYS));
        firstEqualLaunch = saveLaunch("equal-first", "First equal launch", CURRENT_TIME.plus(2, ChronoUnit.DAYS));
        secondEqualLaunch = saveLaunch("equal-second", "Second equal launch", CURRENT_TIME.plus(2, ChronoUnit.DAYS));
        laterEqualLaunch = saveLaunch("equal-later", "Later equal launch", CURRENT_TIME.plus(4, ChronoUnit.DAYS));
        lowerScoreLaunch = saveLaunch("lower", "Lower score launch", CURRENT_TIME.plus(1, ChronoUnit.DAYS));

        Launch pastLaunch = saveLaunch("past", "Past launch", CURRENT_TIME.minus(1, ChronoUnit.HOURS));
        Launch outsidePeriod = saveLaunch("outside", "Outside period", CURRENT_TIME.plus(8, ChronoUnit.DAYS));

        saveLaunch("missing-weather", "Launch without weather", CURRENT_TIME.plus(1, ChronoUnit.DAYS));

        saveWeather(bestLaunch, (short) 95);
        saveWeather(firstEqualLaunch, (short) 90);
        saveWeather(secondEqualLaunch, (short) 90);
        saveWeather(laterEqualLaunch, (short) 90);
        saveWeather(lowerScoreLaunch, (short) 70);
        saveWeather(pastLaunch, (short) 100);
        saveWeather(outsidePeriod, (short) 100);
    }

    @Test
    void findBestViewingLaunches_filtersAndSortsLaunches() {
        List<Launch> result = launchRepository.findBestViewingLaunches(
                        CURRENT_TIME, CURRENT_TIME.plus(7, ChronoUnit.DAYS), PageRequest.of(0, 10));

        assertEquals(List.of(
                        bestLaunch.getId(),
                        firstEqualLaunch.getId(),
                        secondEqualLaunch.getId(),
                        laterEqualLaunch.getId(),
                        lowerScoreLaunch.getId()
                ),
                result.stream()
                        .map(Launch::getId)
                        .toList()
        );
    }

    @Test
    void findBestViewingLaunches_respectsLimit() {
        List<Launch> result = launchRepository.findBestViewingLaunches(CURRENT_TIME, CURRENT_TIME.plus(7,
                ChronoUnit.DAYS), PageRequest.of(0, 2));

        assertEquals(2, result.size());
        assertEquals(bestLaunch.getId(), result.getFirst().getId());
        assertEquals(firstEqualLaunch.getId(), result.getLast().getId());
    }

    @Test
    void findBestViewingLaunches_returnsEmptyListWhenNothingMatches() {
        List<Launch> result = launchRepository.findBestViewingLaunches(CURRENT_TIME.plus(20, ChronoUnit.DAYS),
                        CURRENT_TIME.plus(21, ChronoUnit.DAYS), PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    private Launch saveLaunch(String externalId, String name, Instant launchTime) {
        LaunchDetails details = new LaunchDetails(
                externalId,
                name,
                null,
                LaunchStatus.GO,
                launchTime,
                null,
                null,
                "Test Rocket",
                null,
                "Test Organization",
                "Test Pad",
                "Test Location",
                new BigDecimal("28.500000"),
                new BigDecimal("-80.600000"),
                CURRENT_TIME.minus(1, ChronoUnit.DAYS)
        );

        return launchRepository.save(new Launch(details));
    }

    private void saveWeather(Launch launch, short viewingScore) {
        WeatherDetails details = new WeatherDetails(
                launch.getLaunchTime(),
                new BigDecimal("20.00"),
                (short) 10,
                (short) 5,
                new BigDecimal("12.00"),
                20_000,
                viewingScore,
                CURRENT_TIME
        );

        weatherRepository.save(new WeatherSnapshot(launch, details)
        );
    }
}