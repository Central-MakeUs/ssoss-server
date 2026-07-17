package com.ssoss.ssossbackend.auth.infrastructure.oauth;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.domain.model.SocialProfile;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

record NaverProfileResponse(String resultcode, String message, Response response) {

    record Response(String id, String email) {
    }

    SocialProfile toProfile() {
        if (response == null || response.id() == null) {
            throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }
        return new SocialProfile(response.id(), response.email());
    }
}
