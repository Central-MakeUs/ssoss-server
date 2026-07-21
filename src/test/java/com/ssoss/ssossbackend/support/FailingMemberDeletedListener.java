package com.ssoss.ssossbackend.support;

import java.util.HashSet;
import java.util.Set;

import com.ssoss.ssossbackend.member.application.event.MemberDeletedEvent;

import org.springframework.context.event.EventListener;

public class FailingMemberDeletedListener {

    private final Set<Long> failingMemberIds = new HashSet<>();

    public void failFor(Long memberId) {
        failingMemberIds.add(memberId);
    }

    public void reset() {
        failingMemberIds.clear();
    }

    @EventListener
    void failOnPurpose(MemberDeletedEvent event) {
        if (failingMemberIds.contains(event.memberId())) {
            throw new IllegalStateException("삭제 리스너 실패 시뮬레이션: memberId=" + event.memberId());
        }
    }
}
