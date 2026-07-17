package com.ssoss.ssossbackend.auth.infrastructure.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.ssoss.ssossbackend.shared.exception.ErrorCode;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(errorCode));
    }
}
