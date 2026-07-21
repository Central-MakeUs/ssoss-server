package com.ssoss.ssossbackend.auth.domain.contract;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;

import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    List<RefreshToken> findAllByMemberId(Long memberId);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    int deleteAllByExpiresAtBefore(Instant threshold);

    int deleteAllByMemberId(Long memberId);
}
