package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.dto.LaunchNoteResponse;
import com.launchwindow.service.note.LaunchNoteCommandService;
import com.launchwindow.service.note.LaunchNoteQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LaunchNoteController.class)
@Import(SecurityConfiguration.class)
class LaunchNoteControllerCreateTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LaunchNoteQueryService queryService;

    @MockitoBean
    private LaunchNoteCommandService commandService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void authenticatedUserCanCreateNote() throws Exception {
        LaunchNoteResponse response = new LaunchNoteResponse(
                10L,
                4L,
                "Watch the webcast.",
                null,
                null
        );

        when(commandService.createNote("launch_test", 4L, "Watch the webcast."))
                .thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/launches/4/notes").with(jwt().jwt(token -> token.subject("launch_test")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Watch the webcast."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.launchId").value(4))
                .andExpect(jsonPath("$.content").value("Watch the webcast."));
    }

    @Test
    void blankNoteReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/launches/4/notes")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": " "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingLaunchReturnsNotFound() throws Exception {
        when(commandService.createNote(anyString(), anyLong(), anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/launches/99/notes")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Test note"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Launch with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/api/launches/99/notes"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }
}