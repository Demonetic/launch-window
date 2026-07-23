package com.launchwindow.service.calendar;

import com.launchwindow.dto.CalendarEntryResponse;
import com.launchwindow.dto.CalendarParticipantResponse;
import com.launchwindow.dto.LaunchSummaryResponse;
import com.launchwindow.dto.WeatherSummaryResponse;
import com.launchwindow.model.CalendarEntry;
import com.launchwindow.model.Launch;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CalendarEntryMapper {
    public CalendarEntryResponse map(CalendarEntry entry, WeatherSummaryResponse weather, List<CalendarParticipantResponse> participants) {
        return new CalendarEntryResponse(entry.getId(), entry.getSavedAt(), mapLaunch(entry.getLaunch(), weather), participants);
    }

    private LaunchSummaryResponse mapLaunch(Launch launch, WeatherSummaryResponse weather) {
        return new LaunchSummaryResponse(
                launch.getId(),
                launch.getName(),
                launch.getStatus(),
                launch.getLaunchTime(),
                launch.getImageUrl(),
                launch.getRocketName(),
                launch.getOrganizationName(),
                launch.getPadName(),
                launch.getLocationName(),
                weather
        );
    }
}