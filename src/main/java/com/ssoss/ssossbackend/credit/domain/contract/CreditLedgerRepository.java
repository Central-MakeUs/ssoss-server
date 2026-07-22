package com.ssoss.ssossbackend.credit.domain.contract;

import java.time.Instant;
import java.util.List;

import com.ssoss.ssossbackend.credit.domain.model.CreditLedgerEntry;

import org.springframework.data.repository.CrudRepository;

public interface CreditLedgerRepository extends CrudRepository<CreditLedgerEntry, Long> {

    List<CreditLedgerEntry> findAllByMemberIdAndCreatedAtGreaterThanEqual(Long memberId, Instant cycleStartsAt);
}
