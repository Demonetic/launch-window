package com.launchwindow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LaunchWindowBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LaunchWindowBackendApplication.class, args);
    }

}
