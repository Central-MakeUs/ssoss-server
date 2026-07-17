package com.ssoss.ssossbackend.auth.domain.contract;

import java.util.Optional;

import com.ssoss.ssossbackend.auth.domain.model.AccessTokenPayload;

public interface TokenParser {

    Optional<AccessTokenPayload> parse(String accessToken);
}
