package com.launchwindow.config;

import tools.jackson.databind.ObjectMapper;
import com.launchwindow.dto.ApiErrorCode;
import com.launchwindow.dto.ApiErrorResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RestSecurityErrorHandlerTest {
    private ObjectMapper objectMapper;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletOutputStream outputStream;
    private RestSecurityErrorHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = mock(ObjectMapper.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        outputStream = mock(ServletOutputStream.class);

        when(request.getRequestURI()).thenReturn("/api/calendar");
        when(response.getOutputStream()).thenReturn(outputStream);

        handler = new RestSecurityErrorHandler(objectMapper);
    }

    @Test
    void commenceWritesStandardUnauthorizedResponse() throws Exception {
        AuthenticationException exception = mock(AuthenticationException.class);

        handler.commence(request, response, exception);

        ArgumentCaptor<ApiErrorResponse> errorCaptor = ArgumentCaptor.forClass(ApiErrorResponse.class);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValue(eq(outputStream), errorCaptor.capture());

        ApiErrorResponse error = errorCaptor.getValue();

        assertEquals(401, error.status());
        assertEquals(ApiErrorCode.UNAUTHORIZED, error.code());
        assertEquals("Authentication is required", error.message());
        assertEquals("/api/calendar", error.path());
        assertEquals(0, error.fieldErrors().size());
    }

    @Test
    void handleWritesStandardAccessDeniedResponse() throws Exception {
        AccessDeniedException exception = new AccessDeniedException("Denied");

        handler.handle(request, response, exception);

        ArgumentCaptor<ApiErrorResponse> errorCaptor = ArgumentCaptor.forClass(ApiErrorResponse.class);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValue(eq(outputStream), errorCaptor.capture());

        ApiErrorResponse error = errorCaptor.getValue();

        assertEquals(403, error.status());
        assertEquals(ApiErrorCode.ACCESS_DENIED, error.code());
        assertEquals("Access is denied", error.message());
        assertEquals("/api/calendar", error.path());
        assertEquals(0, error.fieldErrors().size());
    }
}