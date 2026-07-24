package com.launchwindow.repository;

import com.launchwindow.model.AppUser;
import com.launchwindow.model.CalendarEntry;
import com.launchwindow.model.CalendarInvitation;
import com.launchwindow.model.CalendarInvitationStatus;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchDetails;
import com.launchwindow.model.LaunchNote;
import com.launchwindow.model.LaunchStatus;
import com.launchwindow.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class LaunchNoteSharingRepositoryTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-24T08:00:00Z");

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
    void acceptedParticipantCanSeeNotesFromWholeSharedGroup() {
        AppUser owner = saveUser("anna", "anna@example.com");
        AppUser firstParticipant = saveUser("alex", "alex@example.com");
        AppUser secondParticipant = saveUser("sam", "sam@example.com");
        AppUser outsider = saveUser("outsider", "outsider@example.com");

        Launch launch = saveLaunch("shared-launch");

        CalendarEntry entry = calendarRepository.save(new CalendarEntry(owner, launch));

        CalendarInvitation firstInvitation = new CalendarInvitation(entry, owner, firstParticipant);

        firstInvitation.accept(CURRENT_TIME);

        CalendarInvitation secondInvitation = new CalendarInvitation(entry, owner, secondParticipant);

        secondInvitation.accept(CURRENT_TIME);

        invitationRepository.save(firstInvitation);
        invitationRepository.save(secondInvitation);

        LaunchNote ownerNote = noteRepository.save(new LaunchNote(owner, launch, "Owner note"));

        LaunchNote firstParticipantNote =
                noteRepository.save(new LaunchNote(firstParticipant, launch, "First participant note"));

        LaunchNote secondParticipantNote =
                noteRepository.save(new LaunchNote(secondParticipant, launch, "Second participant note"));

        noteRepository.save(new LaunchNote(outsider, launch, "Private outsider note"));

        noteRepository.flush();

        List<LaunchNote> result =
                noteRepository.findAccessibleByLaunchId(firstParticipant.getId(), launch.getId(), CalendarInvitationStatus.ACCEPTED);

        assertThat(result)
                .extracting(LaunchNote::getId)
                .containsExactlyInAnyOrder(ownerNote.getId(), firstParticipantNote.getId(), secondParticipantNote.getId());
    }

    @Test
    void pendingInvitationDoesNotShareNotes() {
        AppUser owner = saveUser("anna", "anna@example.com");
        AppUser invitee = saveUser("alex", "alex@example.com");

        Launch launch = saveLaunch("pending-launch");

        CalendarEntry entry = calendarRepository.save(new CalendarEntry(owner, launch));

        invitationRepository.save(new CalendarInvitation(entry, owner, invitee));

        noteRepository.save(new LaunchNote(owner, launch, "Owner note"));

        LaunchNote inviteeNote = noteRepository.save(new LaunchNote(invitee, launch, "Invitee note"));

        noteRepository.flush();

        List<LaunchNote> result =
                noteRepository.findAccessibleByLaunchId(invitee.getId(), launch.getId(), CalendarInvitationStatus.ACCEPTED);

        assertThat(result)
                .extracting(LaunchNote::getId)
                .containsExactly(inviteeNote.getId());
    }

    @Test
    void userWithoutSharedCalendarOnlySeesOwnNote() {
        AppUser user = saveUser("anna", "anna@example.com");
        AppUser otherUser = saveUser("alex", "alex@example.com");

        Launch launch = saveLaunch("private-launch");

        LaunchNote ownNote = noteRepository.save(new LaunchNote(user, launch, "My note"));

        noteRepository.save(new LaunchNote(otherUser, launch, "Other private note"));

        noteRepository.flush();

        List<LaunchNote> result =
                noteRepository.findAccessibleByLaunchId(user.getId(), launch.getId(), CalendarInvitationStatus.ACCEPTED);

        assertThat(result)
                .extracting(LaunchNote::getId)
                .containsExactly(ownNote.getId());
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