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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LaunchNoteController.class)
@Import(SecurityConfiguration.class)
class LaunchNoteControllerUpdateDeleteTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LaunchNoteQueryService queryService;

    @MockitoBean
    private LaunchNoteCommandService commandService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void authenticatedUserCanUpdateOwnNote() throws Exception {
        LaunchNoteResponse response = new LaunchNoteResponse(
                10L,
                4L,
                "Updated note.",
                null,
                null
        );

        when(commandService.updateNote("launch_test", 10L, "Updated note."))
                .thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/notes/10")
                        .with(jwt().jwt(token -> token.subject("launch_test")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Updated note."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated note."));
    }

    @Test
    void unownedNoteCannotBeUpdated() throws Exception {
        when(commandService.updateNote("launch_test", 99L, "Updated note."))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/notes/99")
                        .with(jwt().jwt(token -> token.subject("launch_test")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Updated note."
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Note with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/api/notes/99"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void authenticatedUserCanDeleteOwnNote() throws Exception {
        when(commandService.deleteNote("launch_test", 10L)).thenReturn(true);

        mockMvc.perform(delete("/api/notes/10")
                        .with(jwt().jwt(token -> token.subject("launch_test"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void unownedNoteCannotBeDeleted() throws Exception {
        when(commandService.deleteNote("launch_test", 99L)).thenReturn(false);

        mockMvc.perform(delete("/api/notes/99")
                        .with(jwt().jwt(token -> token.subject("launch_test"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Note with id 99 was not found"))
                .andExpect(jsonPath("$.path").value("/api/notes/99"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }
}