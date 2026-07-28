package com.launchwindow.repository;

import com.launchwindow.model.AppUser;
import com.launchwindow.model.CalendarInvitation;
import com.launchwindow.model.CalendarInvitationStatus;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchNote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static com.launchwindow.testsupport.AppUserTestData.user;
import static com.launchwindow.testsupport.CalendarTestData.acceptedInvitation;
import static com.launchwindow.testsupport.CalendarTestData.calendarEntry;
import static com.launchwindow.testsupport.CalendarTestData.pendingInvitation;
import static com.launchwindow.testsupport.LaunchNoteTestData.launchNote;
import static com.launchwindow.testsupport.LaunchTestData.launch;
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
        AppUser owner = userRepository.save(user("anna", "anna@example.com"));
        AppUser firstParticipant = userRepository.save(user("alex", "alex@example.com"));
        AppUser secondParticipant = userRepository.save(user("sam", "sam@example.com"));
        AppUser outsider = userRepository.save(user("outsider", "outsider@example.com"));

        Launch launch = launchRepository.save(launch("shared-launch", CURRENT_TIME.plusSeconds(3600)));

        var entry = calendarRepository.save(calendarEntry(owner, launch));
        CalendarInvitation firstInvitation = acceptedInvitation(entry, owner, firstParticipant, CURRENT_TIME);
        CalendarInvitation secondInvitation = acceptedInvitation(entry, owner, secondParticipant, CURRENT_TIME);

        invitationRepository.save(firstInvitation);
        invitationRepository.save(secondInvitation);

        LaunchNote ownerNote = noteRepository.save(launchNote(owner, launch, "Owner note"));

        LaunchNote firstParticipantNote =
                noteRepository.save(launchNote(firstParticipant, launch, "First participant note"));

        LaunchNote secondParticipantNote =
                noteRepository.save(launchNote(secondParticipant, launch, "Second participant note"));

        noteRepository.save(launchNote(outsider, launch, "Private outsider note"));

        noteRepository.flush();

        List<LaunchNote> result =
                noteRepository.findAccessibleByLaunchId(firstParticipant.getId(), launch.getId(), CalendarInvitationStatus.ACCEPTED);

        assertThat(result)
                .extracting(LaunchNote::getId)
                .containsExactlyInAnyOrder(ownerNote.getId(), firstParticipantNote.getId(), secondParticipantNote.getId());
    }

    @Test
    void pendingInvitationDoesNotShareNotes() {
        AppUser owner = userRepository.save(user("anna", "anna@example.com"));
        AppUser invitee = userRepository.save(user("alex", "alex@example.com"));

        Launch launch = launchRepository.save(launch("pending-launch", CURRENT_TIME.plusSeconds(3600)));

        var entry = calendarRepository.save(calendarEntry(owner, launch));

        invitationRepository.save(pendingInvitation(entry, owner, invitee));

        noteRepository.save(launchNote(owner, launch, "Owner note"));

        LaunchNote inviteeNote = noteRepository.save(launchNote(invitee, launch, "Invitee note"));

        noteRepository.flush();

        List<LaunchNote> result =
                noteRepository.findAccessibleByLaunchId(invitee.getId(), launch.getId(), CalendarInvitationStatus.ACCEPTED);

        assertThat(result)
                .extracting(LaunchNote::getId)
                .containsExactly(inviteeNote.getId());
    }

    @Test
    void userWithoutSharedCalendarOnlySeesOwnNote() {
        AppUser user = userRepository.save(user("anna", "anna@example.com"));
        AppUser otherUser = userRepository.save(user("alex", "alex@example.com"));

        Launch launch = launchRepository.save(launch("private-launch", CURRENT_TIME.plusSeconds(3600)));

        LaunchNote ownNote = noteRepository.save(launchNote(user, launch, "My note"));

        noteRepository.save(launchNote(otherUser, launch, "Other private note"));

        noteRepository.flush();

        List<LaunchNote> result =
                noteRepository.findAccessibleByLaunchId(user.getId(), launch.getId(), CalendarInvitationStatus.ACCEPTED);

        assertThat(result)
                .extracting(LaunchNote::getId)
                .containsExactly(ownNote.getId());
    }

}
