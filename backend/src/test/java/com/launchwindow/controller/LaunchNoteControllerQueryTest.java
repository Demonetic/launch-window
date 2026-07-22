package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.LaunchNoteCursor;
import com.launchwindow.dto.LaunchNotePageResponse;
import com.launchwindow.service.note.LaunchNoteCommandService;
import com.launchwindow.service.note.LaunchNoteQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LaunchNoteController.class)
@Import(SecurityConfiguration.class)
class LaunchNoteControllerQueryTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LaunchNoteQueryService queryService;

    @MockitoBean
    private LaunchNoteCommandService commandService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void authenticatedUserCanGetOwnLaunchNotes() throws Exception {
        when(queryService.getNotes("launch_test", 4L)).thenReturn(List.of());

        mockMvc.perform(get("/api/launches/4/notes")
                        .with(jwt().jwt(token -> token
                                .subject("launch_test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(queryService).getNotes("launch_test", 4L);
    }

    @Test
    void authenticatedUserCanGetInitialNotesPage() throws Exception {
        LaunchNotePageResponse response = new LaunchNotePageResponse(List.of(), null, false);

        when(queryService.getNotesPage("launch_test", null, null, 20)).thenReturn(response);

        mockMvc.perform(get("/api/notes")
                        .with(jwt().jwt(token -> token
                                .subject("launch_test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.nextCursor").isEmpty())
                .andExpect(jsonPath("$.hasNext").value(false));

        verify(queryService).getNotesPage("launch_test", null, null, 20);
    }

    @Test
    void authenticatedUserCanGetNotesCursorPage() throws Exception {
        Instant cursorTime = Instant.parse("2026-07-22T10:00:00Z");

        LaunchNotePageResponse response =
                new LaunchNotePageResponse(List.of(), new LaunchNoteCursor(cursorTime, 14L), true);

        when(queryService.getNotesPage("launch_test", cursorTime, 14L, 10)).thenReturn(response);

        mockMvc.perform(get("/api/notes")
                        .with(jwt().jwt(token -> token
                                .subject("launch_test")))
                        .param("limit", "10")
                        .param("beforeUpdatedAt", cursorTime.toString())
                        .param("beforeId", "14"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.nextCursor.beforeUpdatedAt")
                                .value(cursorTime.toString())
                )
                .andExpect(
                        jsonPath("$.nextCursor.beforeId")
                                .value(14)
                )
                .andExpect(jsonPath("$.hasNext").value(true));

        verify(queryService).getNotesPage("launch_test", cursorTime, 14L, 10);
    }

    @Test
    void anonymousUserCannotGetLaunchNotes() throws Exception {
        mockMvc.perform(get("/api/launches/4/notes")).andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousUserCannotGetNotesPage() throws Exception {
        mockMvc.perform(get("/api/notes")).andExpect(status().isUnauthorized());
    }
}