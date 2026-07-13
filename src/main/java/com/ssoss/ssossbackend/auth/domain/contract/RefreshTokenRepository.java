package com.ssoss.ssossbackend.auth.domain.contract;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    List<RefreshToken> findAllByMemberId(Long memberId);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
        UPDATE refresh_token
        SET status = 'DELETED', deleted_at = :now, version = version + 1, updated_at = :now
        WHERE status = 'ACTIVE' AND expires_at < :now
        """)
    int expireAll(@Param("now") Instant now);
}
