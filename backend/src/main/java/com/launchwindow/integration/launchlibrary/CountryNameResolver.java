package com.launchwindow.integration.launchlibrary;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CountryNameResolver {
    public String resolve(String countryCode) {
        String normalizedCode = normalize(countryCode);

        if (normalizedCode == null) {
            return null;
        }

        for (String alpha2Code : Locale.getISOCountries()) {
            Locale country = new Locale.Builder()
                    .setRegion(alpha2Code)
                    .build();

            if (normalizedCode.equals(country.getISO3Country())) {
                return country.getDisplayCountry(Locale.ENGLISH);
            }
        }

        return null;
    }

    public String normalize(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return null;
        }

        String normalized = countryCode
                .trim()
                .toUpperCase(Locale.ROOT);

        if (normalized.length() != 3 || normalized.equals("???")) {
            return null;
        }

        return normalized;
    }
}