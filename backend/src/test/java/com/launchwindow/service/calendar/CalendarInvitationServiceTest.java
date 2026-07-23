package com.launchwindow.service.calendar;

import com.launchwindow.dto.CalendarInvitationResponse;
import com.launchwindow.dto.CreateCalendarInvitationRequest;
import com.launchwindow.exception.InvalidCalendarInvitationException;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.model.*;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.CalendarEntryRepository;
import com.launchwindow.repository.CalendarInvitationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CalendarInvitationServiceTest {
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-23T20:00:00Z");

    @Test
    void invite_createsPendingInvitation() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        CalendarEntryRepository calendarRepository = mock(CalendarEntryRepository.class);
        CalendarInvitationRepository invitationRepository = mock(CalendarInvitationRepository.class);

        CalendarInvitationService service = createService(userRepository, calendarRepository, invitationRepository);

        AppUser inviter = mock(AppUser.class);
        AppUser invitee = mock(AppUser.class);
        CalendarEntry calendarEntry = mock(CalendarEntry.class);
        CalendarInvitation savedInvitation = mock(CalendarInvitation.class);

        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(inviter));
        when(inviter.getId()).thenReturn(1L);
        when(calendarRepository.findByUser_IdAndLaunch_Id(1L, 10L)).thenReturn(Optional.of(calendarEntry));
        when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("alex", "alex")).thenReturn(Optional.of(invitee));
        when(invitee.getId()).thenReturn(2L);
        when(calendarEntry.getId()).thenReturn(20L);
        when(invitationRepository.existsByCalendarEntry_IdAndInvitee_Id(20L, 2L)).thenReturn(false);
        when(invitationRepository.save(any(CalendarInvitation.class))).thenReturn(savedInvitation);

        stubInvitationResponse(savedInvitation, CalendarInvitationStatus.PENDING);

        CalendarInvitationResponse result = service.invite("anna", 10L, new CreateCalendarInvitationRequest("  alex  "));

        assertEquals(50L, result.id());
        assertEquals(10L, result.launchId());
        assertEquals(CalendarInvitationStatus.PENDING, result.status());

        ArgumentCaptor<CalendarInvitation> captor = ArgumentCaptor.forClass(CalendarInvitation.class);

        verify(invitationRepository).save(captor.capture());

        CalendarInvitation created = captor.getValue();

        assertEquals(calendarEntry, created.getCalendarEntry());
        assertEquals(inviter, created.getInviter());
        assertEquals(invitee, created.getInvitee());
        assertEquals(CalendarInvitationStatus.PENDING, created.getStatus());
    }

    @Test
    void invite_cannotInviteSelf() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        CalendarEntryRepository calendarRepository = mock(CalendarEntryRepository.class);
        CalendarInvitationRepository invitationRepository = mock(CalendarInvitationRepository.class);

        CalendarInvitationService service = createService(userRepository, calendarRepository, invitationRepository);
        AppUser user = mock(AppUser.class);
        CalendarEntry calendarEntry = mock(CalendarEntry.class);

        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(user));
        when(calendarRepository.findByUser_IdAndLaunch_Id(1L, 10L)).thenReturn(Optional.of(calendarEntry));
        when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("anna", "anna")).thenReturn(Optional.of(user));

        InvalidCalendarInvitationException exception = assertThrows(
                        InvalidCalendarInvitationException.class,
                        () -> service.invite("anna", 10L, new CreateCalendarInvitationRequest("anna"))
                );

        assertEquals("You cannot invite yourself", exception.getMessage());

        verify(invitationRepository, never()).save(any());
    }

    @Test
    void invite_rejectsDuplicateInvitation() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        CalendarEntryRepository calendarRepository = mock(CalendarEntryRepository.class);
        CalendarInvitationRepository invitationRepository = mock(CalendarInvitationRepository.class);

        CalendarInvitationService service = createService(userRepository, calendarRepository, invitationRepository);
        AppUser inviter = mock(AppUser.class);
        AppUser invitee = mock(AppUser.class);
        CalendarEntry calendarEntry = mock(CalendarEntry.class);

        when(inviter.getId()).thenReturn(1L);
        when(invitee.getId()).thenReturn(2L);
        when(calendarEntry.getId()).thenReturn(20L);
        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(inviter));
        when(calendarRepository.findByUser_IdAndLaunch_Id(1L, 10L)).thenReturn(Optional.of(calendarEntry));
        when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("alex", "alex")).thenReturn(Optional.of(invitee));
        when(invitationRepository.existsByCalendarEntry_IdAndInvitee_Id(20L, 2L)).thenReturn(true);

        InvalidCalendarInvitationException exception = assertThrows(
                        InvalidCalendarInvitationException.class,
                        () -> service.invite("anna", 10L, new CreateCalendarInvitationRequest("alex"))
                );

        assertEquals("This user has already been invited", exception.getMessage());

        verify(invitationRepository, never()).save(any());
    }

    @Test
    void getPendingInvitations_returnsMappedInvitations() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        CalendarEntryRepository calendarRepository = mock(CalendarEntryRepository.class);
        CalendarInvitationRepository invitationRepository = mock(CalendarInvitationRepository.class);

        CalendarInvitationService service = createService(userRepository, calendarRepository, invitationRepository);
        CalendarInvitation invitation = mock(CalendarInvitation.class);

        stubInvitationResponse(invitation, CalendarInvitationStatus.PENDING);

        when(invitationRepository.findAllForInvitee("alex", CalendarInvitationStatus.PENDING)).thenReturn(List.of(invitation));

        List<CalendarInvitationResponse> result = service.getPendingInvitations("alex");

        assertEquals(1, result.size());
        assertEquals(50L, result.getFirst().id());
        assertEquals("Test launch", result.getFirst().launchName());
        assertEquals("anna", result.getFirst().inviterUsername());
    }

    @Test
    void accept_acceptsInvitationAndSavesLaunch() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        CalendarEntryRepository calendarRepository = mock(CalendarEntryRepository.class);
        CalendarInvitationRepository invitationRepository = mock(CalendarInvitationRepository.class);

        CalendarInvitationService service = createService(userRepository, calendarRepository, invitationRepository);
        CalendarInvitation invitation = mock(CalendarInvitation.class);
        CalendarEntry ownerEntry = mock(CalendarEntry.class);
        CalendarEntry inviteeEntry = mock(CalendarEntry.class);
        AppUser invitee = mock(AppUser.class);
        AppUser inviter = mock(AppUser.class);
        Launch launch = mock(Launch.class);

        when(invitationRepository.findForInvitee(50L, "alex")).thenReturn(Optional.of(invitation));
        when(invitation.getStatus()).thenReturn(CalendarInvitationStatus.PENDING, CalendarInvitationStatus.ACCEPTED);
        when(invitation.getCalendarEntry()).thenReturn(ownerEntry);
        when(invitation.getInvitee()).thenReturn(invitee);
        when(invitation.getInviter()).thenReturn(inviter);
        when(ownerEntry.getLaunch()).thenReturn(launch);
        when(invitee.getId()).thenReturn(2L);
        when(launch.getId()).thenReturn(10L);
        when(launch.getName()).thenReturn("Test launch");
        when(launch.getLaunchTime()).thenReturn(Instant.parse("2026-08-01T10:00:00Z"));
        when(inviter.getId()).thenReturn(1L);
        when(inviter.getUsername()).thenReturn("anna");
        when(inviter.getAvatarKey()).thenReturn(AvatarKey.ASTRONAUT);
        when(inviter.getAvatarColor()).thenReturn("#FFFFFF");
        when(invitation.getId()).thenReturn(50L);
        when(invitation.getCreatedAt()).thenReturn(Instant.parse("2026-07-23T19:00:00Z"));
        when(invitation.getRespondedAt()).thenReturn(CURRENT_TIME);
        when(calendarRepository.findByUser_IdAndLaunch_Id(2L, 10L)).thenReturn(Optional.empty());
        when(calendarRepository.save(any(CalendarEntry.class))).thenReturn(inviteeEntry);

        CalendarInvitationResponse result = service.accept("alex", 50L);

        assertEquals(CalendarInvitationStatus.ACCEPTED, result.status());
        assertEquals(CURRENT_TIME, result.respondedAt());

        verify(invitation).accept(CURRENT_TIME);

        ArgumentCaptor<CalendarEntry> captor = ArgumentCaptor.forClass(CalendarEntry.class);

        verify(calendarRepository).save(captor.capture());

        assertEquals(invitee, captor.getValue().getUser());
        assertEquals(launch, captor.getValue().getLaunch());
    }

    @Test
    void decline_declinesInvitationWithoutSavingLaunch() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        CalendarEntryRepository calendarRepository = mock(CalendarEntryRepository.class);
        CalendarInvitationRepository invitationRepository = mock(CalendarInvitationRepository.class);

        CalendarInvitationService service = createService(userRepository, calendarRepository, invitationRepository);
        CalendarInvitation invitation = mock(CalendarInvitation.class);

        when(invitationRepository.findForInvitee(50L, "alex")).thenReturn(Optional.of(invitation));

        when(invitation.getStatus()).thenReturn(CalendarInvitationStatus.PENDING, CalendarInvitationStatus.DECLINED);

        stubInvitationRelations(invitation);

        when(invitation.getRespondedAt()).thenReturn(CURRENT_TIME);

        CalendarInvitationResponse result = service.decline("alex", 50L);

        assertEquals(CalendarInvitationStatus.DECLINED, result.status());
        assertEquals(CURRENT_TIME, result.respondedAt());

        verify(invitation).decline(CURRENT_TIME);
        verifyNoInteractions(calendarRepository);
    }

    @Test
    void accept_unknownInvitationThrowsNotFound() {
        CalendarInvitationRepository invitationRepository = mock(CalendarInvitationRepository.class);

        CalendarInvitationService service = createService(mock(AppUserRepository.class), mock(CalendarEntryRepository.class), invitationRepository);

        when(invitationRepository.findForInvitee(999L, "alex")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.accept("alex", 999L));
    }

    @Test
    void accept_alreadyAnsweredInvitationIsRejected() {
        CalendarInvitationRepository invitationRepository = mock(CalendarInvitationRepository.class);

        CalendarInvitationService service = createService(mock(AppUserRepository.class), mock(CalendarEntryRepository.class), invitationRepository);

        CalendarInvitation invitation = mock(CalendarInvitation.class);

        when(invitationRepository.findForInvitee(50L, "alex")).thenReturn(Optional.of(invitation));
        when(invitation.getStatus()).thenReturn(CalendarInvitationStatus.ACCEPTED);

        InvalidCalendarInvitationException exception =
                assertThrows(InvalidCalendarInvitationException.class, () -> service.accept("alex", 50L));

        assertEquals("Calendar invitation has already been answered", exception.getMessage());
    }

    private CalendarInvitationService createService(AppUserRepository userRepository, CalendarEntryRepository calendarRepository, CalendarInvitationRepository invitationRepository) {
        return new CalendarInvitationService(
                userRepository,
                calendarRepository,
                invitationRepository,
                Clock.fixed(
                        CURRENT_TIME,
                        ZoneOffset.UTC
                )
        );
    }

    private void stubInvitationResponse(CalendarInvitation invitation, CalendarInvitationStatus status) {
        stubInvitationRelations(invitation);

        when(invitation.getStatus()).thenReturn(status);
        when(invitation.getRespondedAt()).thenReturn(null);
    }

    private void stubInvitationRelations(CalendarInvitation invitation) {
        CalendarEntry calendarEntry = mock(CalendarEntry.class);
        Launch launch = mock(Launch.class);
        AppUser inviter = mock(AppUser.class);

        when(invitation.getId()).thenReturn(50L);
        when(invitation.getCalendarEntry()).thenReturn(calendarEntry);
        when(calendarEntry.getLaunch()).thenReturn(launch);
        when(launch.getId()).thenReturn(10L);
        when(launch.getName()).thenReturn("Test launch");
        when(launch.getLaunchTime()).thenReturn(Instant.parse("2026-08-01T10:00:00Z"));
        when(invitation.getInviter()).thenReturn(inviter);
        when(inviter.getId()).thenReturn(1L);
        when(inviter.getUsername()).thenReturn("anna");
        when(inviter.getAvatarKey()).thenReturn(AvatarKey.ASTRONAUT);
        when(inviter.getAvatarColor()).thenReturn("#FFFFFF");
        when(invitation.getCreatedAt()).thenReturn(Instant.parse("2026-07-23T19:00:00Z"));
    }
}