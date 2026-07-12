package com.ssoss.ssossbackend.auth.infrastructure.token;

import java.time.Duration;
import java.time.Instant;

import com.ssoss.ssossbackend.auth.domain.contract.TokenGenerator;
import com.ssoss.ssossbackend.auth.domain.model.LoginToken;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class JwtTokenGenerator implements TokenGenerator {

    private final JwtSigner jwtSigner;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    JwtTokenGenerator(
        JwtSigner jwtSigner,
        @Value("${auth.jwt.access-ttl}") Duration accessTtl,
        @Value("${auth.jwt.refresh-ttl}") Duration refreshTtl
    ) {
        this.jwtSigner = jwtSigner;
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
    }

    @Override
    public LoginToken generate(Long memberId) {
        Instant now = Instant.now();
        return new LoginToken(
            jwtSigner.sign(memberId, now, accessTtl),
            jwtSigner.sign(memberId, now, refreshTtl)
        );
    }
}
