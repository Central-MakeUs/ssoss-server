package com.ssoss.ssossbackend.auth.infrastructure.security;

import java.io.IOException;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ErrorResponseAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorResponseWriter errorResponseWriter;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        errorResponseWriter.write(response, AuthErrorCode.INVALID_ACCESS_TOKEN);
    }
}
