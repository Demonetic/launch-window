package com.launchwindow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCalendarInvitationRequest(
        @NotBlank
        @Size(max = 255)
        String identifier
) {
}