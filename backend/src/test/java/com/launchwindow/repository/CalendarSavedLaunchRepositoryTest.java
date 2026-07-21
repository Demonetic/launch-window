package com.launchwindow.repository;

import com.launchwindow.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class CalendarSavedLaunchRepositoryTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-22T12:00:00Z");

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private LaunchRepository launchRepository;

    @Autowired
    private CalendarEntryRepository calendarRepository;

    @Test
    void findSavedLaunchIdsReturnsOnlyEntriesOwnedByUser() {
        AppUser firstUser = userRepository.save(
                new AppUser(
                        "first-user",
                        "first@example.com",
                        "password-hash",
                        Role.USER
                )
        );

        AppUser secondUser = userRepository.save(
                new AppUser(
                        "second-user",
                        "second@example.com",
                        "password-hash",
                        Role.USER
                )
        );

        Launch firstLaunch = saveLaunch("first-launch");
        Launch secondLaunch = saveLaunch("second-launch");
        Launch thirdLaunch = saveLaunch("third-launch");

        calendarRepository.save(new CalendarEntry(firstUser, firstLaunch));
        calendarRepository.save(new CalendarEntry(firstUser, thirdLaunch));
        calendarRepository.save(new CalendarEntry(secondUser, secondLaunch));

        List<Long> result = calendarRepository.findSavedLaunchIds(
                firstUser.getId(),
                List.of(
                        firstLaunch.getId(),
                        secondLaunch.getId(),
                        thirdLaunch.getId()
                )
        );

        assertThat(result).containsExactlyInAnyOrder(firstLaunch.getId(), thirdLaunch.getId());
    }

    private Launch saveLaunch(String externalId) {
        LaunchDetails details = new LaunchDetails(
                externalId,
                externalId,
                null,
                LaunchStatus.GO,
                CURRENT_TIME.plusSeconds(3600),
                null,
                null,
                "Test rocket",
                null,
                null,
                null,
                null,
                null,
                null,
                CURRENT_TIME
        );

        return launchRepository.save(new Launch(details));
    }
}