package com.launchwindow.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "launch-library")
@Validated
public record LaunchLibraryProperties(
        @NotBlank String baseUrl,
        @Min(1) @Max(100) int pageSize
) {
}
