package com.launchwindow.service.calendar;

import com.launchwindow.dto.CalendarEntryResponse;
import com.launchwindow.dto.CalendarParticipantResponse;
import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.dto.WeatherSummaryResponse;
import com.launchwindow.model.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CalendarEntryMapperTest {

    @Test
    void shouldMapCalendarEntryWithLaunchSummary() {
        CalendarEntry entry = mock(CalendarEntry.class);
        Launch launch = mock(Launch.class);
        List<CalendarParticipantResponse> participants =
                List.of(new CalendarParticipantResponse(1L, "anna", AvatarKey.ASTRONAUT, "#FFFFFF"),
                        new CalendarParticipantResponse(2L, "alex", AvatarKey.ALIEN, "#9FE0C0")
                );

        Instant savedAt = Instant.parse("2026-07-19T16:45:00Z");
        Instant launchTime = Instant.parse("2026-07-23T14:00:00Z");

        when(entry.getId()).thenReturn(1L);
        when(entry.getSavedAt()).thenReturn(savedAt);
        when(entry.getLaunch()).thenReturn(launch);

        when(launch.getId()).thenReturn(4L);
        when(launch.getName()).thenReturn("Starlink Launch");
        when(launch.getStatus()).thenReturn(LaunchStatus.GO);
        when(launch.getLaunchTime()).thenReturn(launchTime);
        when(launch.getImageUrl()).thenReturn("https://example.com/image.jpg");
        when(launch.getRocketName()).thenReturn("Falcon 9");
        when(launch.getOrganizationName()).thenReturn("SpaceX");
        when(launch.getPadName()).thenReturn("SLC-40");
        when(launch.getLocationName()).thenReturn("Cape Canaveral");

        WeatherSummaryResponse weather = new WeatherSummaryResponse((short) 85, ViewingCondition.EXCELLENT,
                        Instant.parse("2026-07-23T14:00:00Z"));

        LaunchSummaryResponse launchResponse =
                new LaunchSummaryResponse(
                        4L,
                        "Starlink Launch",
                        LaunchStatus.GO,
                        launchTime,
                        "https://example.com/image.jpg",
                        "Falcon 9",
                        "SpaceX",
                        "SLC-40",
                        "Cape Canaveral",
                        weather
                );

        CalendarEntryResponse expected = new CalendarEntryResponse(1L, savedAt, launchResponse, participants);

        assertEquals(expected, new CalendarEntryMapper().map(entry, weather, participants));
    }
}