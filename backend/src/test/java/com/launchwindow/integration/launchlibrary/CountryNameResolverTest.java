package com.launchwindow.integration.launchlibrary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CountryNameResolverTest {
    private final CountryNameResolver resolver = new CountryNameResolver();

    @Test
    void resolvesNormalizedIsoAlphaThreeCode() {
        assertEquals("United States", resolver.resolve(" usa "));
    }

    @Test
    void resolvesOtherSupportedCountry() {
        assertEquals("Kazakhstan", resolver.resolve("KAZ"));
    }

    @Test
    void rejectsMissingAndUnknownCountryCodes() {
        assertNull(resolver.resolve(null));
        assertNull(resolver.resolve(" "));
        assertNull(resolver.resolve("???"));
        assertNull(resolver.resolve("INVALID"));
    }
}