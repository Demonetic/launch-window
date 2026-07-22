package com.launchwindow.dto;

import com.launchwindow.model.LaunchStatus;

import java.util.Set;

public record LaunchBrowseFilter(
        LaunchSort sort,
        Integer days,
        Set<LaunchStatus> statuses,
        String query,
        Boolean forecastAvailable,
        Short minimumViewingScore
) {
    public LaunchBrowseFilter {
        sort = sort == null
                ? LaunchSort.SOONEST
                : sort;

        statuses = statuses == null
                ? Set.of()
                : Set.copyOf(statuses);

        query = query == null || query.isBlank()
                ? null
                : query.trim();
    }
}