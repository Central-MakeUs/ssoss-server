package com.ssoss.ssossbackend.credit.domain.contract;

import java.util.Collection;

import com.ssoss.ssossbackend.credit.domain.model.CreditLedger;
import com.ssoss.ssossbackend.credit.domain.model.CreditLedgerType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

public interface CreditLedgerRepository extends ListCrudRepository<CreditLedger, Long> {

    Page<CreditLedger> findAllByMemberIdAndTypeIn(Long memberId, Collection<CreditLedgerType> types,
        Pageable pageable);

    void deleteAllByMemberId(Long memberId);
}
