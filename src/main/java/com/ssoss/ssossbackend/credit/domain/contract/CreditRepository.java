package com.ssoss.ssossbackend.credit.domain.contract;

import java.util.Optional;

import com.ssoss.ssossbackend.credit.domain.model.Credit;

import org.springframework.data.repository.CrudRepository;

public interface CreditRepository extends CrudRepository<Credit, Long> {

    Optional<Credit> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
