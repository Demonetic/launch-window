package com.launchwindow.integration.launchlibrary;

import com.launchwindow.integration.launchlibrary.dto.LaunchLibraryStatusDto;
import com.launchwindow.model.LaunchStatus;
import org.springframework.stereotype.Component;

@Component
public class LaunchStatusMapper {
    public LaunchStatus map(LaunchLibraryStatusDto status) {
        if (status == null) {
            return LaunchStatus.UNKNOWN;
        }

        return switch (status.id()) {
            case 1 -> LaunchStatus.GO;
            case 2 -> LaunchStatus.TO_BE_DETERMINED;
            case 3 -> LaunchStatus.SUCCESS;
            case 4 -> LaunchStatus.FAILURE;
            case 5 -> LaunchStatus.HOLD;
            case 6 -> LaunchStatus.IN_FLIGHT;
            case 7 -> LaunchStatus.PARTIAL_FAILURE;
            case 8 -> LaunchStatus.TO_BE_CONFIRMED;
            default -> LaunchStatus.UNKNOWN;
        };
    }
}
