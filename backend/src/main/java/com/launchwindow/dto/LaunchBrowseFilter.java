package com.launchwindow.dto;

import com.launchwindow.model.LaunchStatus;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public record LaunchBrowseFilter(
        LaunchSort sort,
        Integer days,
        Set<LaunchStatus> statuses,
        Set<String> countryCodes,
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

        countryCodes = countryCodes == null
                ? Set.of()
                : countryCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(code -> code
                        .trim()
                        .toUpperCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());

        query = query == null || query.isBlank()
                ? null
                : query.trim();
    }

    public LaunchBrowseFilter(
            LaunchSort sort,
            Integer days,
            Set<LaunchStatus> statuses,
            String query,
            Boolean forecastAvailable,
            Short minimumViewingScore
    ) {
        this(
                sort,
                days,
                statuses,
                Set.of(),
                query,
                forecastAvailable,
                minimumViewingScore
        );
    }
}