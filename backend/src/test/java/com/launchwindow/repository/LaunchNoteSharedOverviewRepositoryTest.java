package com.launchwindow.repository;

import com.launchwindow.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

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
        AppUser owner = saveUser("anna", "anna@example.com");
        AppUser participant = saveUser("alex", "alex@example.com");
        AppUser outsider = saveUser("outsider", "outsider@example.com");

        Launch sharedLaunch = saveLaunch("shared-launch");
        Launch privateLaunch = saveLaunch("private-launch");

        CalendarEntry sharedEntry = calendarRepository.save(new CalendarEntry(owner, sharedLaunch));

        CalendarInvitation invitation = new CalendarInvitation(sharedEntry, owner, participant);

        invitation.accept(CURRENT_TIME);
        invitationRepository.save(invitation);

        LaunchNote ownerSharedNote = noteRepository.save(new LaunchNote(owner, sharedLaunch, "Owner shared note"));

        LaunchNote participantSharedNote = noteRepository.save(new LaunchNote(participant, sharedLaunch, "Participant shared note"));

        LaunchNote participantPrivateNote = noteRepository.save(new LaunchNote(participant, privateLaunch, "Participant private note"));

        LaunchNote outsiderNote = noteRepository.save(new LaunchNote(outsider, sharedLaunch, "Outsider note"));

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

    private AppUser saveUser(String username, String email) {
        return userRepository.save(new AppUser(username, email, "password-hash", Role.USER));
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
                "Test organization",
                "Test pad",
                "Test location",
                "USA",
                "United States",
                null,
                null,
                CURRENT_TIME
        );

        return launchRepository.save(new Launch(details));
    }
}