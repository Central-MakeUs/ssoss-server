package com.ssoss.ssossbackend.credit.entrypoint.listener;

import com.ssoss.ssossbackend.credit.application.service.CreditService;
import com.ssoss.ssossbackend.member.application.event.MemberDeletedEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreditMemberDeletedListener {

    private final CreditService creditService;

    @EventListener
    public void deleteCredits(MemberDeletedEvent event) {
        creditService.deleteAllByMemberId(event.memberId());
    }
}
