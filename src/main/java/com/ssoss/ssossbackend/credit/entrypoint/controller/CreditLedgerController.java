package com.ssoss.ssossbackend.credit.entrypoint.controller;

import com.ssoss.ssossbackend.credit.application.service.CreditLedgerResult;
import com.ssoss.ssossbackend.credit.application.service.CreditService;
import com.ssoss.ssossbackend.credit.entrypoint.request.CreditLedgerListRequest;
import com.ssoss.ssossbackend.credit.entrypoint.response.CreditLedgerListResponse;
import com.ssoss.ssossbackend.credit.entrypoint.response.CreditLedgerResponse;
import com.ssoss.ssossbackend.shared.paging.PagedResult;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class CreditLedgerController implements CreditLedgerApi {

    private final CreditService creditService;

    @Override
    @GetMapping("/v1/credits/me/ledgers")
    public CreditLedgerListResponse list(
        @AuthenticationPrincipal Long memberId,
        @Valid @ParameterObject CreditLedgerListRequest request
    ) {
        PagedResult<CreditLedgerResult> result = creditService.listLedgers(request.toCommand(memberId));
        return new CreditLedgerListResponse(result.totalCount(), result.page(), result.size(), result.hasNext(),
            result.items().stream()
                .map(ledger -> new CreditLedgerResponse(ledger.ledgerId(), ledger.type(), ledger.description(),
                    ledger.amount(), ledger.occurredAt()))
                .toList());
    }
}
