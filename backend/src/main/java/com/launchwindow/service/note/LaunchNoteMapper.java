package com.launchwindow.service.note;

import com.launchwindow.dto.LaunchNoteOverviewResponse;
import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.LaunchNote;
import org.springframework.stereotype.Component;

@Component
public class LaunchNoteMapper {
    public LaunchNoteResponse map(LaunchNote note) {
        AppUser author = note.getUser();

        return new LaunchNoteResponse(
                note.getId(),
                note.getLaunch().getId(),
                author.getId(),
                author.getUsername(),
                author.getAvatarKey(),
                author.getAvatarColor(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    public LaunchNoteOverviewResponse mapOverview(LaunchNote note) {
        AppUser author = note.getUser();

        return new LaunchNoteOverviewResponse(
                note.getId(),
                note.getLaunch().getId(),
                note.getLaunch().getName(),
                note.getLaunch().getLaunchTime(),
                note.getLaunch().getOrganizationName(),
                note.getLaunch().getImageUrl(),
                author.getId(),
                author.getUsername(),
                author.getAvatarKey(),
                author.getAvatarColor(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}