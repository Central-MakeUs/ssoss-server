package com.ssoss.ssossbackend.credit.application.service;

import com.ssoss.ssossbackend.credit.domain.model.CreditLedgerTab;

import org.springframework.util.StringUtils;

public record CreditLedgerListCommand(Long memberId, CreditLedgerTab tab, int page, int size) {

    public static CreditLedgerListCommand of(Long memberId, String tab, int page, int size) {
        return new CreditLedgerListCommand(
            memberId, StringUtils.hasText(tab) ? CreditLedgerTab.from(tab) : CreditLedgerTab.ALL, page, size);
    }
}
