package com.launchwindow.repository;

import com.launchwindow.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static com.launchwindow.testsupport.AppUserTestData.user;
import static com.launchwindow.testsupport.LaunchNoteTestData.launchNote;
import static com.launchwindow.testsupport.LaunchTestData.launch;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
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

        List<LaunchNote> result = noteRepository.findOverviewInitial(notes.user().getId(), CalendarInvitationStatus.ACCEPTED,
                true, true, PageRequest.of(0, 10));

        assertThat(result).extracting(LaunchNote::getId).containsExactly(notes.second().getId(), notes.first().getId(), notes.older().getId());

        assertThat(result).extracting(note -> note.getLaunch().getName()).containsExactly("Second launch", "First launch", "Older launch");
    }

    @Test
    void overviewCursorUsesUpdatedTimeAndIdTieBreaker() {
        TestNotes notes = saveTestNotes();

        List<LaunchNote> result = noteRepository.findOverviewPage(notes.user().getId(), CalendarInvitationStatus.ACCEPTED, true,
                true, SHARED_UPDATED_AT, notes.second().getId(), PageRequest.of(0, 10));

        assertThat(result).extracting(LaunchNote::getId).containsExactly(notes.first().getId(), notes.older().getId());
    }

    private TestNotes saveTestNotes() {
        AppUser user = userRepository.save(user("notes-user", "notes@example.com"));
        AppUser otherUser = userRepository.save(user("other-user", "other@example.com"));

        Launch firstLaunch =
                launchRepository.save(launch("first-launch", "First launch", LaunchStatus.GO, LAUNCH_TIME));
        Launch secondLaunch =
                launchRepository.save(launch("second-launch", "Second launch", LaunchStatus.GO, LAUNCH_TIME));
        Launch olderLaunch =
                launchRepository.save(launch("older-launch", "Older launch", LaunchStatus.GO, LAUNCH_TIME));
        Launch otherLaunch =
                launchRepository.save(launch("other-launch", "Other launch", LaunchStatus.GO, LAUNCH_TIME));

        LaunchNote first = noteRepository.save(launchNote(user, firstLaunch, "First note"));
        LaunchNote second = noteRepository.save(launchNote(user, secondLaunch, "Second note"));
        LaunchNote older = noteRepository.save(launchNote(user, olderLaunch, "Older note"));

        LaunchNote other = noteRepository.save(launchNote(otherUser, otherLaunch, "Other user's note"));

        noteRepository.flush();

        setUpdatedAt(first.getId(), SHARED_UPDATED_AT);
        setUpdatedAt(second.getId(), SHARED_UPDATED_AT);
        setUpdatedAt(older.getId(), OLDER_UPDATED_AT);
        setUpdatedAt(other.getId(), SHARED_UPDATED_AT.plusSeconds(3600));

        entityManager.flush();
        entityManager.clear();

        return new TestNotes(user, first, second, older);
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
