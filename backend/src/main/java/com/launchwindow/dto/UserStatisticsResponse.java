package com.launchwindow.dto;

public record UserStatisticsResponse(
        long savedLaunches,
        long notesWritten,
        long friends
) {
}