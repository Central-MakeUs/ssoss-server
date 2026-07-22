package com.ssoss.ssossbackend.credit.domain.contract;

import com.ssoss.ssossbackend.credit.domain.model.CreditLedger;

import org.springframework.data.repository.CrudRepository;

public interface CreditLedgerRepository extends CrudRepository<CreditLedger, Long> {

    void deleteAllByMemberId(Long memberId);
}
