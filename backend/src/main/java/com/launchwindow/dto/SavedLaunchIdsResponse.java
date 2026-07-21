package com.launchwindow.dto;

import java.util.List;

public record SavedLaunchIdsResponse(List<Long> savedLaunchIds) {
    public SavedLaunchIdsResponse {
        savedLaunchIds = List.copyOf(savedLaunchIds);
    }
}