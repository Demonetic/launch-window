package com.launchwindow.service.note;

import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.model.LaunchNote;
import org.springframework.stereotype.Component;
import com.launchwindow.dto.LaunchNoteOverviewResponse;

@Component
public class LaunchNoteMapper {
    public LaunchNoteResponse map(LaunchNote note) {
        return new LaunchNoteResponse(
                note.getId(),
                note.getLaunch().getId(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    public LaunchNoteOverviewResponse mapOverview(LaunchNote note) {
        return new LaunchNoteOverviewResponse(
                note.getId(),
                note.getLaunch().getId(),
                note.getLaunch().getName(),
                note.getLaunch().getLaunchTime(),
                note.getLaunch().getOrganizationName(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
