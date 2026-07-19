package com.ssoss.ssossbackend.member.domain.contract;

import java.time.Instant;

import com.ssoss.ssossbackend.member.domain.model.MemberWithdrawalHistory;
import com.ssoss.ssossbackend.member.domain.model.SocialProvider;

import org.springframework.data.repository.CrudRepository;

public interface MemberWithdrawalHistoryRepository extends CrudRepository<MemberWithdrawalHistory, Long> {

    boolean existsByProviderAndSocialIdAndWithdrawnAtAfter(SocialProvider provider, String socialId,
        Instant threshold);

    int deleteAllByWithdrawnAtBefore(Instant threshold);
}
