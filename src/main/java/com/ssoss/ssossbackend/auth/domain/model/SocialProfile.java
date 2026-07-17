package com.ssoss.ssossbackend.auth.domain.model;

import com.ssoss.ssossbackend.shared.exception.BusinessException;

public record SocialProfile(String socialId, String email) {

    public String emailForSignup() {
        if (email == null || email.isBlank()) {
            throw new BusinessException(AuthErrorCode.SIGNUP_EMAIL_NOT_PROVIDED);
        }
        return email;
    }
}
