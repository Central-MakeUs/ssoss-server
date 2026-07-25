package com.ssoss.ssossbackend.auth.domain.contract;

import java.util.Optional;

import com.ssoss.ssossbackend.auth.domain.model.SocialLogin;

import org.springframework.data.repository.CrudRepository;

public interface SocialLoginRepository extends CrudRepository<SocialLogin, Long> {

    Optional<SocialLogin> findByMemberId(Long memberId);

    int deleteAllByMemberId(Long memberId);
}
