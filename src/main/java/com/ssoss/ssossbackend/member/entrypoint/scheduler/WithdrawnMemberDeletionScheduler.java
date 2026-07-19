package com.ssoss.ssossbackend.member.entrypoint.scheduler;

import com.ssoss.ssossbackend.member.application.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawnMemberDeletionScheduler {

    private final MemberService memberService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void deleteWithdrawnMembers() {
        int deleted = memberService.deleteWithdrawn();
        log.info("복구 유예 기간이 지난 탈퇴 회원 삭제: {}건", deleted);
    }
}
