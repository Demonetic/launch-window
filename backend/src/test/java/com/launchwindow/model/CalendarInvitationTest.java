package com.launchwindow.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class CalendarInvitationTest {

    @Test
    void newInvitation_hasPendingStatus() {
        CalendarEntry calendarEntry = mock(CalendarEntry.class);
        AppUser inviter = mock(AppUser.class);
        AppUser invitee = mock(AppUser.class);
        CalendarInvitation invitation = new CalendarInvitation(calendarEntry, inviter, invitee);

        assertEquals(calendarEntry, invitation.getCalendarEntry());
        assertEquals(inviter, invitation.getInviter());
        assertEquals(invitee, invitation.getInvitee());
        assertEquals(CalendarInvitationStatus.PENDING, invitation.getStatus());
        assertNull(invitation.getRespondedAt());
    }

    @Test
    void accept_setsAcceptedStatusAndResponseTime() {
        CalendarInvitation invitation = createInvitation();

        Instant respondedAt = Instant.parse("2026-07-23T18:00:00Z");

        invitation.accept(respondedAt);

        assertEquals(CalendarInvitationStatus.ACCEPTED, invitation.getStatus());
        assertEquals(respondedAt, invitation.getRespondedAt());
    }

    @Test
    void decline_setsDeclinedStatusAndResponseTime() {
        CalendarInvitation invitation = createInvitation();

        Instant respondedAt = Instant.parse("2026-07-23T19:00:00Z");

        invitation.decline(respondedAt);

        assertEquals(CalendarInvitationStatus.DECLINED, invitation.getStatus());
        assertEquals(respondedAt, invitation.getRespondedAt());
    }

    private CalendarInvitation createInvitation() {
        return new CalendarInvitation(
                mock(CalendarEntry.class),
                mock(AppUser.class),
                mock(AppUser.class)
        );
    }
}