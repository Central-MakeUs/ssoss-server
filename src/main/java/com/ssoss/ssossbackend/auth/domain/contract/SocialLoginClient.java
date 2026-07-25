package com.ssoss.ssossbackend.auth.domain.contract;

import java.util.Optional;

import com.ssoss.ssossbackend.auth.domain.model.SocialProfile;
import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;

public interface SocialLoginClient {

    SocialProvider provider();

    SocialProfile fetchProfile(String accessToken);

    Optional<String> exchangeRefreshToken(String credential);

    void unlink(String refreshToken);
}
