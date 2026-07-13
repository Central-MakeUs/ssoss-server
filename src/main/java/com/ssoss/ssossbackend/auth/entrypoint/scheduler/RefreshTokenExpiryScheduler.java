package com.ssoss.ssossbackend.auth.entrypoint.scheduler;

import com.ssoss.ssossbackend.auth.application.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenExpiryScheduler {

    private final RefreshTokenService refreshTokenService;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void expireRefreshTokens() {
        int expired = refreshTokenService.expire();
        log.info("만료 refresh token 일괄 처리: {}건", expired);
    }
}
