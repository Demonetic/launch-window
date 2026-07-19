package com.launchwindow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LaunchNoteRequest(
        @NotBlank
        @Size(max = 5000)
        String content
) {
}
