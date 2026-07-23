package com.launchwindow.repository;

import com.launchwindow.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
        AppUser inviter = saveUser("anna", "anna@example.com");
        AppUser invitee = saveUser("alex", "alex@example.com");

        Launch pendingLaunch = saveLaunch("pending-launch");
        Launch acceptedLaunch = saveLaunch("accepted-launch");
        CalendarEntry pendingEntry = calendarRepository.save(new CalendarEntry(inviter, pendingLaunch));
        CalendarEntry acceptedEntry = calendarRepository.save(new CalendarEntry(inviter, acceptedLaunch));
        CalendarInvitation pendingInvitation = invitationRepository.save(new CalendarInvitation(pendingEntry, inviter, invitee));

        CalendarInvitation acceptedInvitation = new CalendarInvitation(acceptedEntry, inviter, invitee);

        acceptedInvitation.accept(CURRENT_TIME);

        invitationRepository.save(acceptedInvitation);
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
        AppUser inviter = saveUser("anna", "anna@example.com");
        AppUser invitee = saveUser("alex", "alex@example.com");
        AppUser otherUser = saveUser("other", "other@example.com");

        Launch launch = saveLaunch("test-launch");

        CalendarEntry entry = calendarRepository.save(new CalendarEntry(inviter, launch));
        CalendarInvitation invitation = invitationRepository.saveAndFlush(new CalendarInvitation(entry, inviter, invitee));
        Optional<CalendarInvitation> recipientResult = invitationRepository.findForInvitee(invitation.getId(), "alex");
        Optional<CalendarInvitation> otherResult = invitationRepository.findForInvitee(invitation.getId(), otherUser.getUsername());

        assertTrue(recipientResult.isPresent());
        assertTrue(otherResult.isEmpty());
    }

    @Test
    void duplicateInvitationForEntryAndInviteeIsRejected() {
        AppUser inviter = saveUser("anna", "anna@example.com");
        AppUser invitee = saveUser("alex", "alex@example.com");

        Launch launch = saveLaunch("test-launch");

        CalendarEntry entry = calendarRepository.save(new CalendarEntry(inviter, launch));

        invitationRepository.saveAndFlush(new CalendarInvitation(entry, inviter, invitee));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> invitationRepository.saveAndFlush(new CalendarInvitation(entry, inviter, invitee)));
    }

    @Test
    void findAcceptedGroupsForUser_returnsAllMembersOfSharedEntry() {
        AppUser owner = saveUser("anna", "anna@example.com");
        AppUser firstInvitee = saveUser("alex", "alex@example.com");
        AppUser secondInvitee = saveUser("sam", "sam@example.com");
        Launch launch = saveLaunch("shared-launch");
        CalendarEntry entry = calendarRepository.save(new CalendarEntry(owner, launch));
        CalendarInvitation firstInvitation = new CalendarInvitation(entry, owner, firstInvitee);

        firstInvitation.accept(CURRENT_TIME);

        CalendarInvitation secondInvitation = new CalendarInvitation(entry, owner, secondInvitee);

        secondInvitation.accept(CURRENT_TIME);

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