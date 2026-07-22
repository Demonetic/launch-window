package com.launchwindow.repository;

import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchDetails;
import com.launchwindow.model.LaunchStatus;
import com.launchwindow.model.WeatherDetails;
import com.launchwindow.model.WeatherSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class LaunchBrowseRepositoryTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-22T12:00:00Z");

    @Autowired
    private LaunchRepository launchRepository;

    @Autowired
    private WeatherSnapshotRepository weatherRepository;

    @Test
    void soonestSortOrdersByLaunchTimeAndIdAndSupportsCursor() {
        saveLaunch("past", "Past launch", LaunchStatus.GO, CURRENT_TIME.minus(1, ChronoUnit.HOURS));

        Instant equalTime = CURRENT_TIME.plus(1, ChronoUnit.DAYS);

        Launch first = saveLaunch("first", "First launch", LaunchStatus.GO, equalTime);
        Launch second = saveLaunch("second", "Second launch", LaunchStatus.GO, equalTime);
        Launch later = saveLaunch("later", "Later launch", LaunchStatus.GO, CURRENT_TIME.plus(2, ChronoUnit.DAYS));

        List<Launch> firstPage = launchRepository.findBrowseSoonestPage(CURRENT_TIME, null, false,
                Set.of(LaunchStatus.GO), false, Set.of("___"),null, null,
                null, null, null, PageRequest.of(0, 10)
        );

        assertIds(firstPage, first, second, later);

        List<Launch> nextPage = launchRepository.findBrowseSoonestPage(CURRENT_TIME, null, false,
                Set.of(LaunchStatus.GO), false, Set.of("___"),null, null,
                null, equalTime, first.getId(), PageRequest.of(0, 10)
        );

        assertIds(nextPage, second, later);
    }

    @Test
    void soonestSortAppliesPeriodStatusAndSearchFilters() {
        Launch matching = saveLaunch("matching", "Falcon weather mission", LaunchStatus.GO, CURRENT_TIME.plus(2, ChronoUnit.DAYS));

        saveLaunch("wrong-status", "Falcon delayed mission", LaunchStatus.TO_BE_CONFIRMED, CURRENT_TIME.plus(1, ChronoUnit.DAYS));
        saveLaunch("wrong-query", "Electron mission", LaunchStatus.GO, CURRENT_TIME.plus(1, ChronoUnit.DAYS));
        saveLaunch("outside-period", "Falcon outside period", LaunchStatus.GO, CURRENT_TIME.plus(8, ChronoUnit.DAYS));

        List<Launch> result = launchRepository.findBrowseSoonestPage(CURRENT_TIME, CURRENT_TIME.plus(7, ChronoUnit.DAYS), true,
                Set.of(LaunchStatus.GO), false, Set.of("___"),"%falcon%", null,
                null, null, null, PageRequest.of(0, 10)
        );

        assertIds(result, matching);
    }

    @Test
    void soonestSortFiltersByWeatherAvailabilityAndMinimumScore() {
        Launch excellent = saveLaunch("excellent", "Excellent weather", LaunchStatus.GO, CURRENT_TIME.plus(1, ChronoUnit.DAYS));
        Launch poor = saveLaunch("poor", "Poor weather", LaunchStatus.GO, CURRENT_TIME.plus(2, ChronoUnit.DAYS));
        Launch missing = saveLaunch("missing", "Missing weather", LaunchStatus.GO, CURRENT_TIME.plus(3, ChronoUnit.DAYS));

        saveWeather(excellent, (short) 90);
        saveWeather(poor, (short) 40);

        List<Launch> withGoodForecast = launchRepository.findBrowseSoonestPage(CURRENT_TIME, null, false,
                Set.of(LaunchStatus.GO), false, Set.of("___"),null, true, (short) 70,
                null, null, PageRequest.of(0, 10)
        );

        assertIds(withGoodForecast, excellent);

        List<Launch> withoutForecast = launchRepository.findBrowseSoonestPage(CURRENT_TIME, null, false,
                Set.of(LaunchStatus.GO), false, Set.of("___"),null, false,
                null, null, null, PageRequest.of(0, 10)
        );

        assertIds(withoutForecast, missing);
    }

    @Test
    void bestViewingSortOrdersByScoreTimeAndIdWithMissingWeatherLast() {
        Launch best = saveLaunch("best", "Best viewing", LaunchStatus.GO, CURRENT_TIME.plus(3, ChronoUnit.DAYS));

        Instant equalTime = CURRENT_TIME.plus(1, ChronoUnit.DAYS);

        Launch firstEqual = saveLaunch("equal-first", "First equal score", LaunchStatus.GO, equalTime);
        Launch secondEqual = saveLaunch("equal-second", "Second equal score", LaunchStatus.GO, equalTime);
        Launch lower = saveLaunch("lower", "Lower score", LaunchStatus.GO, CURRENT_TIME.plus(2, ChronoUnit.DAYS));
        Launch missing = saveLaunch("missing-best", "Missing forecast", LaunchStatus.GO, CURRENT_TIME.plus(1, ChronoUnit.HOURS));

        saveWeather(best, (short) 95);
        saveWeather(firstEqual, (short) 80);
        saveWeather(secondEqual, (short) 80);
        saveWeather(lower, (short) 30);

        List<Launch> result = launchRepository.findBrowseBestViewingPage(CURRENT_TIME, null, false,
                Set.of(LaunchStatus.GO), false, Set.of("___"),null, null,
                null, null, null, null, PageRequest.of(0, 10)
        );

        assertIds(result, best, firstEqual, secondEqual, lower, missing);
    }

    @Test
    void bestViewingSortContinuesAfterCompleteCompositeCursor() {
        Launch best = saveLaunch("cursor-best", "Best score", LaunchStatus.GO, CURRENT_TIME.plus(3, ChronoUnit.DAYS));

        Instant equalTime = CURRENT_TIME.plus(1, ChronoUnit.DAYS);

        Launch firstEqual = saveLaunch("cursor-equal-first", "First equal score", LaunchStatus.GO, equalTime);
        Launch secondEqual = saveLaunch("cursor-equal-second", "Second equal score", LaunchStatus.GO, equalTime);
        Launch lower = saveLaunch("cursor-lower", "Lower score", LaunchStatus.GO, CURRENT_TIME.plus(2, ChronoUnit.DAYS));
        Launch missing = saveLaunch("cursor-missing", "Missing forecast", LaunchStatus.GO, CURRENT_TIME.plus(4, ChronoUnit.DAYS));

        saveWeather(best, (short) 95);
        saveWeather(firstEqual, (short) 80);
        saveWeather(secondEqual, (short) 80);
        saveWeather(lower, (short) 30);

        List<Launch> result = launchRepository.findBrowseBestViewingPage(CURRENT_TIME, null, false,
                Set.of(LaunchStatus.GO), false, Set.of("___"),null, null,
                null, (short) 80, equalTime, firstEqual.getId(), PageRequest.of(0, 10)
        );

        assertIds(result, secondEqual, lower, missing);
    }

    @Test
    void soonestSortFiltersByMultipleCountries() {
        Launch usa = saveLaunch("country-usa", "USA launch", LaunchStatus.GO,
                CURRENT_TIME.plus(1, ChronoUnit.DAYS), "USA", "United States");

        Launch china = saveLaunch("country-china", "China launch", LaunchStatus.GO,
                CURRENT_TIME.plus(2, ChronoUnit.DAYS), "CHN", "China");

        saveLaunch("country-kazakhstan", "Kazakhstan launch", LaunchStatus.GO,
                CURRENT_TIME.plus(3, ChronoUnit.DAYS), "KAZ", "Kazakhstan");

        List<Launch> result =
                launchRepository.findBrowseSoonestPage(
                        CURRENT_TIME,
                        null,
                        false,
                        Set.of(LaunchStatus.GO),
                        true,
                        Set.of("USA", "CHN"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 10)
                );

        assertIds(result, usa, china);
    }

    private Launch saveLaunch(String externalId, String name, LaunchStatus status, Instant launchTime) {
        return saveLaunch(
                externalId,
                name,
                status,
                launchTime,
                null,
                null
        );
    }

    private Launch saveLaunch(String externalId, String name, LaunchStatus status, Instant launchTime, String countryCode, String countryName) {
        LaunchDetails details = new LaunchDetails(
                externalId,
                name,
                "Test description",
                status,
                launchTime,
                null,
                null,
                "Test rocket",
                "Test mission",
                "Test organization",
                "Test pad",
                "Test location",
                countryCode,
                countryName,
                new BigDecimal("28.500000"),
                new BigDecimal("-80.600000"),
                CURRENT_TIME
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

        weatherRepository.save(new WeatherSnapshot(launch, details));
    }

    private void assertIds(List<Launch> actual, Launch... expected) {
        assertEquals(
                List.of(expected).stream()
                        .map(Launch::getId)
                        .toList(),
                actual.stream()
                        .map(Launch::getId)
                        .toList()
        );
    }
}