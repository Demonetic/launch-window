package com.launchwindow.repository;

import com.launchwindow.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.launchwindow.testsupport.AppUserTestData.user;
import static com.launchwindow.testsupport.CalendarTestData.acceptedInvitation;
import static com.launchwindow.testsupport.CalendarTestData.calendarEntry;
import static com.launchwindow.testsupport.CalendarTestData.pendingInvitation;
import static com.launchwindow.testsupport.LaunchTestData.launch;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class CalendarInvitationRepositoryTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-23T20:00:00Z");

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private LaunchRepository launchRepository;

    @Autowired
    private CalendarEntryRepository calendarRepository;

    @Autowired
    private CalendarInvitationRepository invitationRepository;

    @Test
    void findAllForInvitee_returnsOnlyPendingInvitations() {
        AppUser inviter = userRepository.save(user("anna", "anna@example.com"));
        AppUser invitee = userRepository.save(user("alex", "alex@example.com"));

        Launch pendingLaunch = launchRepository.save(launch("pending-launch", CURRENT_TIME.plusSeconds(3600)));
        Launch acceptedLaunch = launchRepository.save(launch("accepted-launch", CURRENT_TIME.plusSeconds(3600)));
        CalendarEntry pendingEntry = calendarRepository.save(calendarEntry(inviter, pendingLaunch));
        CalendarEntry acceptedEntry = calendarRepository.save(calendarEntry(inviter, acceptedLaunch));
        CalendarInvitation pendingInvitation =
                invitationRepository.save(pendingInvitation(pendingEntry, inviter, invitee));

        invitationRepository.save(acceptedInvitation(acceptedEntry, inviter, invitee, CURRENT_TIME));
        invitationRepository.flush();

        List<CalendarInvitation> result = invitationRepository.findAllForInvitee("alex", CalendarInvitationStatus.PENDING);

        assertEquals(1, result.size());
        assertEquals(pendingInvitation.getId(), result.getFirst().getId());
        assertEquals(pendingLaunch.getId(), result.getFirst()
                        .getCalendarEntry()
                        .getLaunch()
                        .getId());
        assertEquals(inviter.getId(), result.getFirst().getInviter().getId());
    }

    @Test
    void findForInvitee_returnsInvitationOnlyForRecipient() {
        AppUser inviter = userRepository.save(user("anna", "anna@example.com"));
        AppUser invitee = userRepository.save(user("alex", "alex@example.com"));
        AppUser otherUser = userRepository.save(user("other", "other@example.com"));

        Launch launch = launchRepository.save(launch("test-launch", CURRENT_TIME.plusSeconds(3600)));

        CalendarEntry entry = calendarRepository.save(calendarEntry(inviter, launch));
        CalendarInvitation invitation =
                invitationRepository.saveAndFlush(pendingInvitation(entry, inviter, invitee));
        Optional<CalendarInvitation> recipientResult = invitationRepository.findForInvitee(invitation.getId(), "alex");
        Optional<CalendarInvitation> otherResult = invitationRepository.findForInvitee(invitation.getId(), otherUser.getUsername());

        assertTrue(recipientResult.isPresent());
        assertTrue(otherResult.isEmpty());
    }

    @Test
    void duplicateInvitationForEntryAndInviteeIsRejected() {
        AppUser inviter = userRepository.save(user("anna", "anna@example.com"));
        AppUser invitee = userRepository.save(user("alex", "alex@example.com"));

        Launch launch = launchRepository.save(launch("test-launch", CURRENT_TIME.plusSeconds(3600)));

        CalendarEntry entry = calendarRepository.save(calendarEntry(inviter, launch));

        invitationRepository.saveAndFlush(pendingInvitation(entry, inviter, invitee));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> invitationRepository.saveAndFlush(pendingInvitation(entry, inviter, invitee)));
    }

    @Test
    void findAcceptedGroupsForUser_returnsAllMembersOfSharedEntry() {
        AppUser owner = userRepository.save(user("anna", "anna@example.com"));
        AppUser firstInvitee = userRepository.save(user("alex", "alex@example.com"));
        AppUser secondInvitee = userRepository.save(user("sam", "sam@example.com"));
        Launch launch = launchRepository.save(launch("shared-launch", CURRENT_TIME.plusSeconds(3600)));
        CalendarEntry entry = calendarRepository.save(calendarEntry(owner, launch));
        CalendarInvitation firstInvitation = acceptedInvitation(entry, owner, firstInvitee, CURRENT_TIME);
        CalendarInvitation secondInvitation = acceptedInvitation(entry, owner, secondInvitee, CURRENT_TIME);

        invitationRepository.save(firstInvitation);
        invitationRepository.save(secondInvitation);
        invitationRepository.flush();

        List<CalendarInvitation> result = invitationRepository.findAcceptedGroupsForUser(
                                firstInvitee.getId(), List.of(launch.getId()), CalendarInvitationStatus.ACCEPTED);

        assertEquals(2, result.size());

        assertEquals(List.of(firstInvitee.getId(), secondInvitee.getId()), result.stream()
                        .map(CalendarInvitation::getInvitee)
                        .map(AppUser::getId)
                        .toList()
        );
    }

}
