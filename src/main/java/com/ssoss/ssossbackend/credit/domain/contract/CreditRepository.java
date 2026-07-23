package com.ssoss.ssossbackend.credit.domain.contract;

import java.util.Optional;

import com.ssoss.ssossbackend.credit.domain.model.Credit;

import org.springframework.data.relational.core.sql.LockMode;
import org.springframework.data.relational.repository.Lock;
import org.springframework.data.repository.ListCrudRepository;

public interface CreditRepository extends ListCrudRepository<Credit, Long> {

    Optional<Credit> findByMemberId(Long memberId);

    @Lock(LockMode.PESSIMISTIC_WRITE)
    Optional<Credit> findWithLockByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
