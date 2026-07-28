package com.launchwindow.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        String identifier,

        @NotBlank
        String password
) {
}