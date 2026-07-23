package com.launchwindow.service.calendar;

import com.launchwindow.dto.CalendarParticipantResponse;
import com.launchwindow.model.*;
import com.launchwindow.repository.CalendarInvitationRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CalendarParticipantQueryServiceTest {

    @Test
    void getByLaunchIds_unsharedLaunchReturnsCurrentUser() {
        CalendarInvitationRepository repository = mock(CalendarInvitationRepository.class);

        CalendarParticipantQueryService service = new CalendarParticipantQueryService(repository);
        AppUser currentUser = user(1L, "anna", AvatarKey.ASTRONAUT, "#FFFFFF");

        when(repository.findAcceptedGroupsForUser(1L, List.of(10L), CalendarInvitationStatus.ACCEPTED)).thenReturn(List.of());

        Map<Long, List<CalendarParticipantResponse>> result = service.getByLaunchIds(currentUser, List.of(10L));

        assertEquals(List.of(new CalendarParticipantResponse(1L, "anna", AvatarKey.ASTRONAUT, "#FFFFFF")),
                result.get(10L));
    }

    @Test
    void getByLaunchIds_acceptedInvitationReturnsBothUsers() {
        CalendarInvitationRepository repository = mock(CalendarInvitationRepository.class);

        CalendarParticipantQueryService service = new CalendarParticipantQueryService(repository);

        AppUser currentUser = user(1L, "anna", AvatarKey.ASTRONAUT, "#FFFFFF");
        AppUser invitedUser = user(2L, "alex", AvatarKey.ALIEN, "#9FE0C0");
        CalendarInvitation invitation = invitation(10L, currentUser, invitedUser);

        when(repository.findAcceptedGroupsForUser(1L, List.of(10L), CalendarInvitationStatus.ACCEPTED)).thenReturn(List.of(invitation));

        Map<Long, List<CalendarParticipantResponse>> result = service.getByLaunchIds(currentUser, List.of(10L));

        assertEquals(List.of(new CalendarParticipantResponse(1L, "anna", AvatarKey.ASTRONAUT, "#FFFFFF"),
                        new CalendarParticipantResponse(2L, "alex", AvatarKey.ALIEN, "#9FE0C0")),
                result.get(10L));
    }

    @Test
    void getByLaunchIds_multipleInvitationsReturnUniqueUsers() {
        CalendarInvitationRepository repository = mock(CalendarInvitationRepository.class);

        CalendarParticipantQueryService service = new CalendarParticipantQueryService(repository);

        AppUser currentUser = user(1L, "anna", AvatarKey.ASTRONAUT, "#FFFFFF");
        AppUser firstInvitee = user(2L, "alex", AvatarKey.ALIEN, "#9FE0C0");
        AppUser secondInvitee = user(3L, "sam", AvatarKey.PLANET, "#D8B4FE");
        CalendarInvitation firstInvitation = invitation(10L, currentUser, firstInvitee);
        CalendarInvitation duplicateInvitation = invitation(10L, currentUser, firstInvitee);
        CalendarInvitation secondInvitation = invitation(10L, currentUser, secondInvitee);

        when(repository.findAcceptedGroupsForUser(1L, List.of(10L), CalendarInvitationStatus.ACCEPTED
        )).thenReturn(List.of(firstInvitation, duplicateInvitation, secondInvitation));

        Map<Long, List<CalendarParticipantResponse>> result = service.getByLaunchIds(currentUser, List.of(10L, 10L));

        assertEquals(3, result.get(10L).size());
        assertEquals(List.of(1L, 2L, 3L), result.get(10L)
                        .stream()
                        .map(CalendarParticipantResponse::userId)
                        .toList()
        );

        verify(repository).findAcceptedGroupsForUser(1L, List.of(10L), CalendarInvitationStatus.ACCEPTED);
    }

    @Test
    void getByLaunchIds_emptyLaunchListReturnsEmptyMap() {
        CalendarInvitationRepository repository = mock(CalendarInvitationRepository.class);

        CalendarParticipantQueryService service = new CalendarParticipantQueryService(repository);

        AppUser currentUser = mock(AppUser.class);

        Map<Long, List<CalendarParticipantResponse>> result = service.getByLaunchIds(currentUser, List.of());

        assertEquals(Map.of(), result);
        verifyNoInteractions(repository);
    }

    private AppUser user(Long id, String username, AvatarKey avatarKey, String avatarColor) {
        AppUser user = mock(AppUser.class);

        when(user.getId()).thenReturn(id);
        when(user.getUsername()).thenReturn(username);
        when(user.getAvatarKey()).thenReturn(avatarKey);
        when(user.getAvatarColor()).thenReturn(avatarColor);

        return user;
    }

    private CalendarInvitation invitation(Long launchId, AppUser inviter, AppUser invitee) {
        Launch launch = mock(Launch.class);
        CalendarEntry calendarEntry = mock(CalendarEntry.class);
        CalendarInvitation invitation = mock(CalendarInvitation.class);

        when(launch.getId()).thenReturn(launchId);
        when(calendarEntry.getLaunch()).thenReturn(launch);
        when(invitation.getCalendarEntry()).thenReturn(calendarEntry);
        when(invitation.getInviter()).thenReturn(inviter);
        when(invitation.getInvitee()).thenReturn(invitee);

        return invitation;
    }
}