package com.launchwindow.controller;

import com.launchwindow.config.SecurityConfiguration;
import com.launchwindow.service.note.LaunchNoteCommandService;
import com.launchwindow.service.note.LaunchNoteQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        mockMvc.perform(get("/api/launches/4/notes").with(jwt().jwt(token -> token
                                .subject("launch_test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(queryService).getNotes("launch_test", 4L);
    }

    @Test
    void anonymousUserCannotGetLaunchNotes() throws Exception {
        mockMvc.perform(get("/api/launches/4/notes")).andExpect(status().isUnauthorized());
    }
}