package com.ssoss.ssossbackend.member.entrypoint.scheduler;

import com.ssoss.ssossbackend.member.application.service.MemberService;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WithdrawnMemberDeletionScheduler {

    private final MemberService memberService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void deleteWithdrawnMembers() {
        memberService.findAllDueForDeletion()
            .forEach(memberService::deleteWithdrawn);
    }
}
