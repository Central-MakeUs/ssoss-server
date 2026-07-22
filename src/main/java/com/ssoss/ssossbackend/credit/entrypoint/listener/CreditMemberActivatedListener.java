package com.ssoss.ssossbackend.credit.entrypoint.listener;

import com.ssoss.ssossbackend.credit.application.service.CreditService;
import com.ssoss.ssossbackend.member.application.event.MemberActivatedEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreditMemberActivatedListener {

    private final CreditService creditService;

    @EventListener
    public void grantCredit(MemberActivatedEvent event) {
        creditService.grant(event.memberId());
    }
}
