package com.ssoss.ssossbackend.auth.infrastructure.security;

import java.util.List;

import com.ssoss.ssossbackend.auth.domain.contract.TokenParser;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AccessTokenAuthenticator {

    private final TokenParser tokenParser;

    void authenticate(String accessToken) {
        tokenParser.parse(accessToken)
            .ifPresent(payload -> SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                    payload.memberId(), null, List.of(new SimpleGrantedAuthority(payload.status().name())))));
    }
}
