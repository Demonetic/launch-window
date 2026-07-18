package com.launchwindow.integration.launchlibrary;

import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryStatusDto;
import com.launchwindow.model.LaunchStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LaunchStatusMapperTest {
    private final LaunchStatusMapper mapper = new LaunchStatusMapper();

    @ParameterizedTest
    @CsvSource({
            "1, GO",
            "2, TO_BE_DETERMINED",
            "3, SUCCESS",
            "4, FAILURE",
            "5, HOLD",
            "6, IN_FLIGHT",
            "7, PARTIAL_FAILURE",
            "8, TO_BE_CONFIRMED"
    })
    void mapsKnownStatus(int id, LaunchStatus expected) {
        LaunchLibraryStatusDto status = new LaunchLibraryStatusDto(id, "Status", "Status");

        assertEquals(expected, mapper.map(status));
    }

    @Test
    void mapsUnknownStatusToUnknown() {
        LaunchLibraryStatusDto status = new LaunchLibraryStatusDto(999, "New status", "New");

        assertEquals(LaunchStatus.UNKNOWN, mapper.map(status));
    }

    @Test
    void mapMissingStatusToUnknown() {
        assertEquals(LaunchStatus.UNKNOWN, mapper.map(null));
    }
}
