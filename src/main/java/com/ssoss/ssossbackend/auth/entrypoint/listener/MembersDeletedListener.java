package com.ssoss.ssossbackend.auth.entrypoint.listener;

import com.ssoss.ssossbackend.auth.application.service.RefreshTokenService;
import com.ssoss.ssossbackend.member.application.event.MembersDeletedEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MembersDeletedListener {

    private final RefreshTokenService refreshTokenService;

    @EventListener
    public void deleteRefreshTokens(MembersDeletedEvent event) {
        refreshTokenService.deleteAllByMemberIds(event.memberIds());
    }
}
