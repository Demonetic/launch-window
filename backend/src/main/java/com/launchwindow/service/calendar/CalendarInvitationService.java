package com.launchwindow.service.calendar;

import com.launchwindow.dto.CalendarInvitationResponse;
import com.launchwindow.dto.CreateCalendarInvitationRequest;
import com.launchwindow.exception.InvalidCalendarInvitationException;
import com.launchwindow.exception.ResourceNotFoundException;
import com.launchwindow.model.*;
import com.launchwindow.repository.AppUserRepository;
import com.launchwindow.repository.CalendarEntryRepository;
import com.launchwindow.repository.CalendarInvitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

@Service
public class CalendarInvitationService {
    private final AppUserRepository userRepository;
    private final CalendarEntryRepository calendarRepository;
    private final CalendarInvitationRepository invitationRepository;
    private final Clock clock;

    public CalendarInvitationService(AppUserRepository userRepository, CalendarEntryRepository calendarRepository,
                                     CalendarInvitationRepository invitationRepository, Clock clock) {
        this.userRepository = userRepository;
        this.calendarRepository = calendarRepository;
        this.invitationRepository = invitationRepository;
        this.clock = clock;
    }

    @Transactional
    public CalendarInvitationResponse invite(String username, Long launchId, CreateCalendarInvitationRequest request) {
        AppUser inviter = findUser(username);

        CalendarEntry calendarEntry = calendarRepository.findByUser_IdAndLaunch_Id(inviter.getId(), launchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Save the launch before inviting another user"));

        String identifier = request.identifier().trim();

        AppUser invitee = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(identifier, identifier)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invited user was not found"));

        validateInvitation(inviter, invitee, calendarEntry);

        CalendarInvitation invitation = invitationRepository.save(
                        new CalendarInvitation(calendarEntry, inviter, invitee));

        return map(invitation);
    }

    @Transactional(readOnly = true)
    public List<CalendarInvitationResponse>
    getPendingInvitations(String username) {
        return invitationRepository.findAllForInvitee(username, CalendarInvitationStatus.PENDING)
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional
    public CalendarInvitationResponse accept(String username, Long invitationId) {
        CalendarInvitation invitation = findPendingInvitation(username, invitationId);

        invitation.accept(clock.instant());

        AppUser invitee = invitation.getInvitee();
        Launch launch = invitation
                .getCalendarEntry()
                .getLaunch();

        calendarRepository.findByUser_IdAndLaunch_Id(invitee.getId(), launch.getId())
                .orElseGet(() -> calendarRepository.save(new CalendarEntry(invitee, launch)));

        return map(invitation);
    }

    @Transactional
    public CalendarInvitationResponse decline(String username, Long invitationId) {
        CalendarInvitation invitation = findPendingInvitation(username, invitationId);

        invitation.decline(clock.instant());

        return map(invitation);
    }

    private AppUser findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Authenticated user was not found"));
    }

    private CalendarInvitation findPendingInvitation(String username, Long invitationId) {
        CalendarInvitation invitation =
                invitationRepository.findForInvitee(invitationId, username)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Calendar invitation was not found"));

        if (invitation.getStatus() != CalendarInvitationStatus.PENDING) {
            throw new InvalidCalendarInvitationException("Calendar invitation has already been answered");
        }

        return invitation;
    }

    private void validateInvitation(AppUser inviter, AppUser invitee, CalendarEntry calendarEntry) {
        if (Objects.equals(inviter.getId(), invitee.getId())) {
            throw new InvalidCalendarInvitationException("You cannot invite yourself");
        }

        if (invitationRepository.existsByCalendarEntry_IdAndInvitee_Id(calendarEntry.getId(), invitee.getId())) {
            throw new InvalidCalendarInvitationException("This user has already been invited");
        }
    }

    private CalendarInvitationResponse map(CalendarInvitation invitation) {
        Launch launch = invitation
                .getCalendarEntry()
                .getLaunch();

        AppUser inviter = invitation.getInviter();

        return new CalendarInvitationResponse(
                invitation.getId(),
                launch.getId(),
                launch.getName(),
                launch.getLaunchTime(),
                inviter.getId(),
                inviter.getUsername(),
                inviter.getAvatarKey(),
                inviter.getAvatarColor(),
                invitation.getStatus(),
                invitation.getCreatedAt(),
                invitation.getRespondedAt()
        );
    }
}