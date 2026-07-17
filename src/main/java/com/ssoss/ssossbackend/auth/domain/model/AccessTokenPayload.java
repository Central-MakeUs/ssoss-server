package com.ssoss.ssossbackend.auth.domain.model;

public record AccessTokenPayload(Long memberId, MemberStatus status) {
}
