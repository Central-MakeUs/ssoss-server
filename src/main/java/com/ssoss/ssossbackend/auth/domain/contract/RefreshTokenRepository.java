package com.ssoss.ssossbackend.auth.domain.contract;

import java.util.Optional;

import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;

import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByMemberId(Long memberId);
}
