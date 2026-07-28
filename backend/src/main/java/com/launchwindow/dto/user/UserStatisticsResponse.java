package com.launchwindow.dto.user;

public record UserStatisticsResponse(
        long savedLaunches,
        long notesWritten,
        long friends
) {
}