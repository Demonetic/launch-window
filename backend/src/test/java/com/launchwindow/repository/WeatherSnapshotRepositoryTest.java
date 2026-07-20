package com.launchwindow.repository;

import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchDetails;
import com.launchwindow.model.LaunchStatus;
import com.launchwindow.model.WeatherDetails;
import com.launchwindow.model.WeatherSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class WeatherSnapshotRepositoryTest {
    @Autowired
    private LaunchRepository launchRepository;

    @Autowired
    private WeatherSnapshotRepository weatherRepository;

    @Test
    void oneLaunchCannotHaveMultipleWeatherSnapshots() {
        Launch launch = launchRepository.save(new Launch(launchDetails()));

        WeatherSnapshot firstSnapshot = weatherRepository.saveAndFlush(
                new WeatherSnapshot(launch, weatherDetails("2026-07-21T12:00:00Z")));

        assertEquals(firstSnapshot, weatherRepository.findByLaunch_Id(launch.getId()).orElseThrow());

        WeatherSnapshot secondSnapshot = new WeatherSnapshot(launch, weatherDetails("2026-07-21T15:00:00Z"));

        assertThrows(DataIntegrityViolationException.class, () -> weatherRepository.saveAndFlush(secondSnapshot));
    }

    private LaunchDetails launchDetails() {
        Instant launchTime = Instant.parse("2026-07-21T14:30:00Z");

        return new LaunchDetails(
                "repository-test-launch",
                "Repository Test Launch",
                null,
                LaunchStatus.GO,
                launchTime,
                null,
                null,
                "Test Rocket",
                null,
                null,
                null,
                null,
                new BigDecimal("28.500000"),
                new BigDecimal("-80.600000"),
                Instant.parse("2026-07-20T12:00:00Z")
        );
    }

    private WeatherDetails weatherDetails(String forecastTime) {
        return new WeatherDetails(
                Instant.parse(forecastTime),
                new BigDecimal("20.00"),
                (short) 10,
                (short) 5,
                new BigDecimal("12.00"),
                20_000,
                (short) 90,
                Instant.parse("2026-07-20T12:00:00Z")
        );
    }
}