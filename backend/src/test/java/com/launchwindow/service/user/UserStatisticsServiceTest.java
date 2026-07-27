package com.launchwindow.service.user;

import com.launchwindow.dto.UserStatisticsResponse;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.FriendshipStatus;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.CalendarEntryRepository;
import com.launchwindow.repository.FriendshipRepository;
import com.launchwindow.repository.LaunchNoteRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserStatisticsServiceTest {

    @Test
    void returnsStatisticsForAuthenticatedUser() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        CalendarEntryRepository calendarRepository = mock(CalendarEntryRepository.class);
        LaunchNoteRepository noteRepository = mock(LaunchNoteRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        AppUser user = mock(AppUser.class);

        UserStatisticsService service = new UserStatisticsService(userRepository, calendarRepository, noteRepository, friendshipRepository);

        when(userRepository.findByUsername("launch_test")).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(7L);
        when(calendarRepository.countByUser_Id(7L)).thenReturn(8L);
        when(noteRepository.countByUser_Id(7L)).thenReturn(14L);
        when(friendshipRepository.countForUserWithStatus(7L, FriendshipStatus.ACCEPTED)).thenReturn(3L);

        UserStatisticsResponse result = service.getStatistics("launch_test");

        assertEquals(new UserStatisticsResponse(8L, 14L, 3L), result
        );
    }

    @Test
    void missingAuthenticatedUserThrowsNotFound() {
        AppUserRepository userRepository = mock(AppUserRepository.class);
        CalendarEntryRepository calendarRepository = mock(CalendarEntryRepository.class);
        LaunchNoteRepository noteRepository = mock(LaunchNoteRepository.class);
        FriendshipRepository friendshipRepository = mock(FriendshipRepository.class);

        UserStatisticsService service = new UserStatisticsService(userRepository, calendarRepository, noteRepository, friendshipRepository);

        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.getStatistics("missing"));

        assertEquals("Authenticated user was not found", exception.getMessage());

        verifyNoInteractions(calendarRepository, noteRepository, friendshipRepository);
    }
}