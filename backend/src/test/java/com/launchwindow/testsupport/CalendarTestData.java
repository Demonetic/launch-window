package com.launchwindow.testsupport;

import com.launchwindow.model.AppUser;
import com.launchwindow.model.CalendarEntry;
import com.launchwindow.model.CalendarInvitation;
import com.launchwindow.model.Launch;

import java.time.Instant;

public final class CalendarTestData {
    private CalendarTestData() {
    }

    public static CalendarEntry calendarEntry(AppUser user, Launch launch) {
        return new CalendarEntry(user, launch);
    }

    public static CalendarInvitation pendingInvitation(CalendarEntry calendarEntry, AppUser inviter, AppUser invitee) {
        return new CalendarInvitation(calendarEntry, inviter, invitee);
    }

    public static CalendarInvitation acceptedInvitation(CalendarEntry calendarEntry, AppUser inviter, AppUser invitee, Instant respondedAt) {
        CalendarInvitation invitation = pendingInvitation(calendarEntry, inviter, invitee);

        invitation.accept(respondedAt);

        return invitation;
    }
}
