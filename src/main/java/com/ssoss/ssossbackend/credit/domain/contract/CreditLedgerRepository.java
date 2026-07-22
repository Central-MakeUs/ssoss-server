package com.ssoss.ssossbackend.credit.domain.contract;

import com.ssoss.ssossbackend.credit.domain.model.CreditLedger;

import org.springframework.data.repository.ListCrudRepository;

public interface CreditLedgerRepository extends ListCrudRepository<CreditLedger, Long> {

    void deleteAllByMemberId(Long memberId);
}
