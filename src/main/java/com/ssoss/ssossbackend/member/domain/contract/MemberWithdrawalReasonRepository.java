package com.ssoss.ssossbackend.member.domain.contract;

import com.ssoss.ssossbackend.member.domain.model.MemberWithdrawalReason;

import org.springframework.data.repository.CrudRepository;

public interface MemberWithdrawalReasonRepository extends CrudRepository<MemberWithdrawalReason, Long> {
}
