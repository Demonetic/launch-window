package com.launchwindow.service.user;

import com.launchwindow.dto.user.UserStatisticsResponse;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.FriendshipStatus;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.CalendarEntryRepository;
import com.launchwindow.repository.FriendshipRepository;
import com.launchwindow.repository.LaunchNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserStatisticsService {
    private final AppUserRepository userRepository;
    private final CalendarEntryRepository calendarEntryRepository;
    private final LaunchNoteRepository launchNoteRepository;
    private final FriendshipRepository friendshipRepository;

    public UserStatisticsService(AppUserRepository userRepository, CalendarEntryRepository calendarEntryRepository,
                                 LaunchNoteRepository launchNoteRepository, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.calendarEntryRepository = calendarEntryRepository;
        this.launchNoteRepository = launchNoteRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @Transactional(readOnly = true)
    public UserStatisticsResponse getStatistics(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));

        Long userId = user.getId();

        return new UserStatisticsResponse(calendarEntryRepository.countByUser_Id(userId), launchNoteRepository.countByUser_Id(userId),
                friendshipRepository.countForUserWithStatus(userId, FriendshipStatus.ACCEPTED));
    }
}