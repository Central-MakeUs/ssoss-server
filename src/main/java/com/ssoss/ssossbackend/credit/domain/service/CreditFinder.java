package com.ssoss.ssossbackend.credit.domain.service;

import java.util.List;
import java.util.Optional;

import com.ssoss.ssossbackend.credit.domain.contract.CreditLedgerRepository;
import com.ssoss.ssossbackend.credit.domain.contract.CreditRepository;
import com.ssoss.ssossbackend.credit.domain.model.Credit;
import com.ssoss.ssossbackend.credit.domain.model.CreditLedger;
import com.ssoss.ssossbackend.credit.domain.model.CreditLedgerTab;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreditFinder {

    private static final Sort OCCURRED_AT_DESC = Sort.by(Sort.Direction.DESC, "createdAt", "id");

    private final CreditRepository creditRepository;
    private final CreditLedgerRepository creditLedgerRepository;

    public Optional<Credit> find(Long memberId) {
        return creditRepository.findByMemberId(memberId);
    }

    public List<Long> findAllMemberIds() {
        return creditRepository.findAll().stream()
            .map(Credit::getMemberId)
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<CreditLedger> listLedgers(Long memberId, CreditLedgerTab tab, int page, int size) {
        return creditLedgerRepository.findAllByMemberIdAndTypeIn(memberId, tab.types(),
            PageRequest.of(page, size, OCCURRED_AT_DESC));
    }
}
