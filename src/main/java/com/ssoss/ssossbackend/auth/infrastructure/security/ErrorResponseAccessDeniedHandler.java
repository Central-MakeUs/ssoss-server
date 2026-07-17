package com.ssoss.ssossbackend.auth.infrastructure.security;

import java.io.IOException;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ErrorResponseAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityErrorResponseWriter errorResponseWriter;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException accessDeniedException) throws IOException {
        errorResponseWriter.write(response, AuthErrorCode.ACCESS_DENIED);
    }
}
