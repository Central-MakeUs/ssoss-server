package com.ssoss.ssossbackend.member.domain.contract;

import com.ssoss.ssossbackend.member.domain.model.MemberWithdrawalHistory;

import org.springframework.data.repository.CrudRepository;

public interface MemberWithdrawalHistoryRepository extends CrudRepository<MemberWithdrawalHistory, Long> {
}
