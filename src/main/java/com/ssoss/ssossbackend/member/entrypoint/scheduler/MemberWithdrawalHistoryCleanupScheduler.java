package com.ssoss.ssossbackend.member.entrypoint.scheduler;

import com.ssoss.ssossbackend.member.application.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberWithdrawalHistoryCleanupScheduler {

    private final MemberService memberService;

    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    public void cleanUpWithdrawalHistories() {
        int deleted = memberService.cleanUpWithdrawalHistories();
        log.info("재가입 제한 기간이 지난 탈퇴 이력 삭제: {}건", deleted);
    }
}
