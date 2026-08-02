package com.ssoss.ssossbackend.member.entrypoint.scheduler;

import com.ssoss.ssossbackend.member.application.service.MemberService;
import com.ssoss.ssossbackend.member.application.service.WithdrawnMemberDeletionResult;

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
        WithdrawnMemberDeletionResult result = memberService.deleteWithdrawnMembers();
        log.info("탈퇴 회원 삭제 배치 완료: 대상 {}명, 삭제 {}명", result.targetCount(), result.deletedCount());
    }
}
