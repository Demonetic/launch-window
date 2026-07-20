package com.launchwindow.service.note;

import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.model.LaunchNote;
import org.springframework.stereotype.Component;

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
}
