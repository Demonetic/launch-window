package com.launchwindow.service.calendar;

import com.launchwindow.dto.CalendarParticipantResponse;
import com.launchwindow.model.AppUser;
import com.launchwindow.model.CalendarInvitation;
import com.launchwindow.model.CalendarInvitationStatus;
import com.launchwindow.repository.CalendarInvitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalendarParticipantQueryService {
    private final CalendarInvitationRepository repository;

    public CalendarParticipantQueryService(CalendarInvitationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Map<Long, List<CalendarParticipantResponse>>
    getByLaunchIds(AppUser currentUser, List<Long> launchIds) {
        if (launchIds.isEmpty()) {
            return Map.of();
        }

        List<Long> distinctLaunchIds = launchIds.stream().distinct().toList();

        Map<Long, LinkedHashMap<Long, CalendarParticipantResponse>> participantsByLaunchId = new LinkedHashMap<>();

        CalendarParticipantResponse currentParticipant = map(currentUser);

        for (Long launchId : distinctLaunchIds) {
            LinkedHashMap<Long, CalendarParticipantResponse> participants = new LinkedHashMap<>();

            participants.put(currentUser.getId(), currentParticipant);

            participantsByLaunchId.put(launchId, participants);
        }

        List<CalendarInvitation> invitations = repository.findAcceptedGroupsForUser(currentUser.getId(), distinctLaunchIds,
                CalendarInvitationStatus.ACCEPTED);

        for (CalendarInvitation invitation : invitations) {
            Long launchId = invitation
                    .getCalendarEntry()
                    .getLaunch()
                    .getId();

            LinkedHashMap<Long, CalendarParticipantResponse> participants = participantsByLaunchId.get(launchId);

            if (participants == null) {
                continue;
            }

            addParticipant(participants, invitation.getInviter());
            addParticipant(participants, invitation.getInvitee());
        }

        Map<Long, List<CalendarParticipantResponse>> result = new LinkedHashMap<>();

        participantsByLaunchId.forEach((launchId, participants) ->
                        result.put(launchId, List.copyOf(participants.values()))
        );

        return result;
    }

    private void addParticipant(
            Map<Long, CalendarParticipantResponse> participants, AppUser user) {
        participants.putIfAbsent(user.getId(), map(user));
    }

    private CalendarParticipantResponse map(AppUser user) {
        return new CalendarParticipantResponse(
                user.getId(),
                user.getUsername(),
                user.getAvatarKey(),
                user.getAvatarColor()
        );
    }
}