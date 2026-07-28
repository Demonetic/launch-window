package com.launchwindow.testsupport;

import com.launchwindow.model.AppUser;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchNote;

public final class LaunchNoteTestData {
    private LaunchNoteTestData() {
    }

    public static LaunchNote launchNote(AppUser user, Launch launch, String content) {
        return new LaunchNote(user, launch, content);
    }
}
