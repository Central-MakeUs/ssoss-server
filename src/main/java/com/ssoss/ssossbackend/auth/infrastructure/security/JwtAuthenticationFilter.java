package com.ssoss.ssossbackend.auth.infrastructure.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AccessTokenAuthenticator accessTokenAuthenticator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            accessTokenAuthenticator.authenticate(header.substring(BEARER_PREFIX.length()));
        }
        filterChain.doFilter(request, response);
    }
}
