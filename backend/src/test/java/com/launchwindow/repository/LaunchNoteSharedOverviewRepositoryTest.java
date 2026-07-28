package com.launchwindow.repository;

import com.launchwindow.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static com.launchwindow.testsupport.AppUserTestData.user;
import static com.launchwindow.testsupport.CalendarTestData.acceptedInvitation;
import static com.launchwindow.testsupport.CalendarTestData.calendarEntry;
import static com.launchwindow.testsupport.LaunchNoteTestData.launchNote;
import static com.launchwindow.testsupport.LaunchTestData.launch;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class LaunchNoteSharedOverviewRepositoryTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-24T09:00:00Z");

    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private LaunchRepository launchRepository;
    @Autowired
    private CalendarEntryRepository calendarRepository;
    @Autowired
    private CalendarInvitationRepository invitationRepository;
    @Autowired
    private LaunchNoteRepository noteRepository;

    @Test
    void overviewFiltersOwnAndFriendsNotes() {
        AppUser owner = userRepository.save(user("anna", "anna@example.com"));
        AppUser participant = userRepository.save(user("alex", "alex@example.com"));
        AppUser outsider = userRepository.save(user("outsider", "outsider@example.com"));

        Launch sharedLaunch = launchRepository.save(launch("shared-launch", CURRENT_TIME.plusSeconds(3600)));
        Launch privateLaunch = launchRepository.save(launch("private-launch", CURRENT_TIME.plusSeconds(3600)));

        CalendarEntry sharedEntry = calendarRepository.save(calendarEntry(owner, sharedLaunch));
        invitationRepository.save(acceptedInvitation(sharedEntry, owner, participant, CURRENT_TIME));

        LaunchNote ownerSharedNote = noteRepository.save(launchNote(owner, sharedLaunch, "Owner shared note"));

        LaunchNote participantSharedNote =
                noteRepository.save(launchNote(participant, sharedLaunch, "Participant shared note"));

        LaunchNote participantPrivateNote =
                noteRepository.save(launchNote(participant, privateLaunch, "Participant private note"));

        LaunchNote outsiderNote = noteRepository.save(launchNote(outsider, sharedLaunch, "Outsider note"));

        noteRepository.flush();

        List<LaunchNote> allNotes =
                noteRepository.findOverviewInitial(participant.getId(), CalendarInvitationStatus.ACCEPTED, true, true,
                        PageRequest.of(0, 20));

        List<LaunchNote> ownNotes =
                noteRepository.findOverviewInitial(participant.getId(), CalendarInvitationStatus.ACCEPTED, true, false,
                        PageRequest.of(0, 20));

        List<LaunchNote> friendsNotes =
                noteRepository.findOverviewInitial(participant.getId(), CalendarInvitationStatus.ACCEPTED, false, true,
                        PageRequest.of(0, 20));

        assertThat(allNotes)
                .extracting(LaunchNote::getId)
                .containsExactlyInAnyOrder(ownerSharedNote.getId(), participantSharedNote.getId(), participantPrivateNote.getId())
                .doesNotContain(outsiderNote.getId());

        assertThat(ownNotes)
                .extracting(LaunchNote::getId)
                .containsExactlyInAnyOrder(participantSharedNote.getId(), participantPrivateNote.getId());

        assertThat(friendsNotes)
                .extracting(LaunchNote::getId)
                .containsExactly(ownerSharedNote.getId())
                .doesNotContain(participantSharedNote.getId(), participantPrivateNote.getId(), outsiderNote.getId());
    }

}
