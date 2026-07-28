package com.launchwindow.testsupport;

import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchDetails;
import com.launchwindow.model.LaunchStatus;

import java.math.BigDecimal;
import java.time.Instant;

public final class LaunchTestData {
    private LaunchTestData() {
    }

    public static Launch launch(String externalId, Instant launchTime) {
        return launch(externalId, externalId, LaunchStatus.GO, launchTime);
    }

    public static Launch launch(String externalId, String name, LaunchStatus status, Instant launchTime) {
        return new Launch(launchDetails(externalId, name, status, launchTime));
    }

    public static Launch launchWithLocation(String externalId, String name, LaunchStatus status, Instant launchTime,
                                            String countryCode, String countryName, BigDecimal latitude, BigDecimal longitude) {
        return new Launch(launchDetails(externalId, name, status, launchTime, countryCode, countryName, latitude, longitude));
    }

    public static LaunchDetails launchDetails(String externalId, String name, LaunchStatus status, Instant launchTime) {
        return launchDetails(externalId, name, status, launchTime, null, null, null, null);
    }

    public static LaunchDetails launchDetails(String externalId, String name, LaunchStatus status, Instant launchTime,
                                              String countryCode, String countryName, BigDecimal latitude, BigDecimal longitude) {
        return new LaunchDetails(externalId, name, null, status, launchTime, null, null,
                "Test rocket", null, "Test organization", "Test pad",
                "Test location", countryCode, countryName, latitude, longitude, launchTime.minusSeconds(3600));
    }
}
