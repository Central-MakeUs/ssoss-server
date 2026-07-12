package com.ssoss.ssossbackend.auth.domain.contract;

import com.ssoss.ssossbackend.auth.domain.model.SocialProfile;
import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;

public interface SocialLoginClient {

    SocialProvider provider();

    SocialProfile fetchProfile(String accessToken);
}
