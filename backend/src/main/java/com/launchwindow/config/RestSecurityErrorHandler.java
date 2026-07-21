package com.launchwindow.config;

import tools.jackson.databind.ObjectMapper;
import com.launchwindow.dto.ApiErrorCode;
import com.launchwindow.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public class RestSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    public RestSecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        writeError(request, response, HttpServletResponse.SC_UNAUTHORIZED, ApiErrorCode.UNAUTHORIZED, "Authentication is required");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
        writeError(request, response, HttpServletResponse.SC_FORBIDDEN, ApiErrorCode.ACCESS_DENIED, "Access is denied");
    }

    private void writeError(HttpServletRequest request, HttpServletResponse response, int status, ApiErrorCode code, String message) throws IOException {
        ApiErrorResponse error = new ApiErrorResponse(Instant.now(), status, code, message, request.getRequestURI(), Map.of());

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}