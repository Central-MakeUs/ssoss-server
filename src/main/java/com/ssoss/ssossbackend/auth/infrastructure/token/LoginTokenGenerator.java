package com.ssoss.ssossbackend.auth.infrastructure.token;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.ssoss.ssossbackend.auth.domain.contract.TokenGenerator;
import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.auth.domain.model.MemberStatus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class LoginTokenGenerator implements TokenGenerator {

    private final JwtSigner jwtSigner;
    private final OpaqueTokenGenerator opaqueTokenGenerator;
    private final Clock clock;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    LoginTokenGenerator(
        JwtSigner jwtSigner,
        OpaqueTokenGenerator opaqueTokenGenerator,
        Clock clock,
        @Value("${auth.access-ttl}") Duration accessTtl,
        @Value("${auth.refresh-ttl}") Duration refreshTtl
    ) {
        this.jwtSigner = jwtSigner;
        this.opaqueTokenGenerator = opaqueTokenGenerator;
        this.clock = clock;
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
    }

    @Override
    public LoginToken generate(Long memberId, MemberStatus status) {
        Instant now = clock.instant();
        return new LoginToken(
            jwtSigner.sign(memberId, status, now, accessTtl),
            opaqueTokenGenerator.generate(),
            now.plus(refreshTtl)
        );
    }
}
