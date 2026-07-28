package com.launchwindow.repository;

import com.launchwindow.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static com.launchwindow.testsupport.AppUserTestData.user;
import static com.launchwindow.testsupport.CalendarTestData.calendarEntry;
import static com.launchwindow.testsupport.LaunchTestData.launch;
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
        AppUser firstUser = userRepository.save(user("first-user", "first@example.com"));
        AppUser secondUser = userRepository.save(user("second-user", "second@example.com"));

        Launch firstLaunch = launchRepository.save(launch("first-launch", CURRENT_TIME.plusSeconds(3600)));
        Launch secondLaunch = launchRepository.save(launch("second-launch", CURRENT_TIME.plusSeconds(3600)));
        Launch thirdLaunch = launchRepository.save(launch("third-launch", CURRENT_TIME.plusSeconds(3600)));

        calendarRepository.save(calendarEntry(firstUser, firstLaunch));
        calendarRepository.save(calendarEntry(firstUser, thirdLaunch));
        calendarRepository.save(calendarEntry(secondUser, secondLaunch));

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

}
