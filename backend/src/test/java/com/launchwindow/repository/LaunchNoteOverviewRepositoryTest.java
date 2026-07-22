package com.launchwindow.repository;

import com.launchwindow.model.AppUser;
import com.launchwindow.model.Launch;
import com.launchwindow.model.LaunchDetails;
import com.launchwindow.model.LaunchNote;
import com.launchwindow.model.LaunchStatus;
import com.launchwindow.model.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class LaunchNoteOverviewRepositoryTest {
    private static final Instant LAUNCH_TIME = Instant.parse("2026-08-01T10:00:00Z");
    private static final Instant CURRENT_TIME = Instant.parse("2026-07-22T12:00:00Z");
    private static final Instant SHARED_UPDATED_AT = Instant.parse("2026-07-22T10:00:00Z");
    private static final Instant OLDER_UPDATED_AT = Instant.parse("2026-07-21T10:00:00Z");

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private LaunchRepository launchRepository;

    @Autowired
    private LaunchNoteRepository noteRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void initialOverviewReturnsOnlyUsersNotesInCursorOrder() {
        TestNotes notes = saveTestNotes();

        List<LaunchNote> result = noteRepository.findOverviewInitial(notes.user().getId(), PageRequest.of(0, 10));

        assertThat(result).extracting(LaunchNote::getId)
                .containsExactly(notes.second().getId(), notes.first().getId(), notes.older().getId());

        assertThat(result).extracting(note -> note.getLaunch().getName())
                .containsExactly("Second launch", "First launch", "Older launch");
    }

    @Test
    void overviewCursorUsesUpdatedTimeAndIdTieBreaker() {
        TestNotes notes = saveTestNotes();

        List<LaunchNote> result = noteRepository.findOverviewPage(notes.user().getId(),
                SHARED_UPDATED_AT, notes.second().getId(), PageRequest.of(0, 10));

        assertThat(result)
                .extracting(LaunchNote::getId)
                .containsExactly(notes.first().getId(), notes.older().getId());
    }

    private TestNotes saveTestNotes() {
        AppUser user = userRepository.save(
                new AppUser("notes-user", "notes@example.com", "password-hash", Role.USER)
        );

        AppUser otherUser = userRepository.save(
                new AppUser("other-user", "other@example.com", "password-hash", Role.USER)
        );

        Launch firstLaunch = saveLaunch("first-launch", "First launch");
        Launch secondLaunch = saveLaunch("second-launch", "Second launch");
        Launch olderLaunch = saveLaunch("older-launch", "Older launch");
        Launch otherLaunch = saveLaunch("other-launch", "Other launch");

        LaunchNote first = noteRepository.save(new LaunchNote(user, firstLaunch, "First note"));
        LaunchNote second = noteRepository.save(new LaunchNote(user, secondLaunch, "Second note"));
        LaunchNote older = noteRepository.save(new LaunchNote(user, olderLaunch, "Older note"));
        LaunchNote other = noteRepository.save(new LaunchNote(otherUser, otherLaunch, "Other user's note"));

        noteRepository.flush();

        setUpdatedAt(first.getId(), SHARED_UPDATED_AT);
        setUpdatedAt(second.getId(), SHARED_UPDATED_AT);
        setUpdatedAt(older.getId(), OLDER_UPDATED_AT);
        setUpdatedAt(other.getId(), SHARED_UPDATED_AT.plusSeconds(3600));

        entityManager.flush();
        entityManager.clear();

        return new TestNotes(user, first, second, older);
    }

    private Launch saveLaunch(String externalId, String name) {
        LaunchDetails details = new LaunchDetails(
                externalId,
                name,
                null,
                LaunchStatus.GO,
                LAUNCH_TIME,
                null,
                null,
                "Test rocket",
                null,
                null,
                null,
                null,
                null,
                null,
                CURRENT_TIME
        );

        return launchRepository.save(new Launch(details));
    }

    private void setUpdatedAt(Long noteId, Instant updatedAt) {
        entityManager.createNativeQuery("""
                        UPDATE launch_note
                        SET updated_at = :updatedAt
                        WHERE id = :noteId
                        """)
                .setParameter("updatedAt", Timestamp.from(updatedAt))
                .setParameter("noteId", noteId)
                .executeUpdate();
    }

    private record TestNotes(
            AppUser user,
            LaunchNote first,
            LaunchNote second,
            LaunchNote older
    ) {
    }
}