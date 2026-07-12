package com.ssoss.ssossbackend.member.domain.contract;

import java.util.Optional;

import com.ssoss.ssossbackend.member.domain.model.Member;
import com.ssoss.ssossbackend.member.domain.model.SocialProvider;

import org.springframework.data.repository.CrudRepository;

public interface MemberRepository extends CrudRepository<Member, Long> {

    Optional<Member> findByProviderAndSocialId(SocialProvider provider, String socialId);
}
