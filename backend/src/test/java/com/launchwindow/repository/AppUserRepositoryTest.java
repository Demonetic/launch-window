package com.launchwindow.repository;

import com.launchwindow.model.AppUser;
import com.launchwindow.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void findByUsernameIgnoreCaseOrEmailIgnoreCase_usernameMatchesIgnoringCase_shouldReturnUser() {
        AppUser user = saveUser();

        var result = appUserRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("SPACEFAN",
                "missing@example.com");

        assertThat(result).contains(user);
    }

    @Test
    void findByUsernameIgnoreCaseOrEmailIgnoreCase_emailMatchesIgnoringCase_shouldReturnUser() {
        AppUser user = saveUser();

        var result = appUserRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("missing-user",
                "SPACE@EXAMPLE.COM");

        assertThat(result).contains(user);
    }

    @Test
    void existsMethods_valuesDifferOnlyByCase_shouldReturnTrue() {
        saveUser();

        assertThat(appUserRepository.existsByUsernameIgnoreCase("SPACEFAN")).isTrue();
        assertThat(appUserRepository.existsByEmailIgnoreCase("SPACE@EXAMPLE.COM")).isTrue();
    }

    private AppUser saveUser() {
        return appUserRepository.save(
                new AppUser("spacefan", "space@example.com", "password-hash", Role.USER)
        );
    }
}