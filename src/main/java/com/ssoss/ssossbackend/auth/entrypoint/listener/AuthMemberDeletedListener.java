package com.ssoss.ssossbackend.auth.entrypoint.listener;

import com.ssoss.ssossbackend.auth.application.service.RefreshTokenService;
import com.ssoss.ssossbackend.auth.application.service.SocialLoginService;
import com.ssoss.ssossbackend.member.event.MemberDeletedEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthMemberDeletedListener {

    private final RefreshTokenService refreshTokenService;
    private final SocialLoginService socialLoginService;

    @EventListener
    public void deleteRefreshTokens(MemberDeletedEvent event) {
        refreshTokenService.deleteAllByMemberId(event.memberId());
    }

    @EventListener
    public void deleteSocialLogin(MemberDeletedEvent event) {
        socialLoginService.delete(event.memberId());
    }
}
