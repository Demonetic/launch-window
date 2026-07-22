package com.launchwindow.repository;

import com.launchwindow.dto.CountryResponse;
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
class LaunchCountryRepositoryTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-22T12:00:00Z");

    @Autowired
    private LaunchRepository repository;

    @Test
    void returnsDistinctUpcomingCountriesAlphabetically() {
        saveLaunch("usa-first", CURRENT_TIME.plus(1, ChronoUnit.DAYS), "USA", "United States");
        saveLaunch("usa-second", CURRENT_TIME.plus(2, ChronoUnit.DAYS), "USA", "United States");
        saveLaunch("china", CURRENT_TIME.plus(3, ChronoUnit.DAYS), "CHN", "China");
        saveLaunch("past", CURRENT_TIME.minus(1, ChronoUnit.DAYS), "KAZ", "Kazakhstan");
        saveLaunch("missing-country", CURRENT_TIME.plus(1, ChronoUnit.DAYS), null, null);

        List<CountryResponse> result = repository.findUpcomingCountries(CURRENT_TIME);

        assertEquals(
                List.of(new CountryResponse("CHN", "China"),
                        new CountryResponse("USA", "United States")
                ),
                result
        );
    }

    private void saveLaunch(String externalId, Instant launchTime, String countryCode, String countryName) {
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
                "Test organization",
                "Test pad",
                "Test location",
                countryCode,
                countryName,
                new BigDecimal("28.500000"),
                new BigDecimal("-80.600000"),
                CURRENT_TIME
        );

        repository.save(new Launch(details));
    }
}