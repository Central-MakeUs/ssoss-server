package com.ssoss.ssossbackend.member.domain.contract;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.ssoss.ssossbackend.member.domain.model.Member;
import com.ssoss.ssossbackend.member.domain.model.MemberStatus;
import com.ssoss.ssossbackend.member.domain.model.SocialProvider;

import org.springframework.data.repository.CrudRepository;

public interface MemberRepository extends CrudRepository<Member, Long> {

    Optional<Member> findByProviderAndSocialId(SocialProvider provider, String socialId);

    List<Member> findAllByStatusAndLastWithdrawnAtBefore(MemberStatus status, Instant threshold);

    int deleteByIdAndStatusAndLastWithdrawnAtBefore(Long id, MemberStatus status, Instant threshold);
}
