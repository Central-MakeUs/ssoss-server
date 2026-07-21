package com.ssoss.ssossbackend.member.domain.model;

import com.ssoss.ssossbackend.shared.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    ALREADY_SIGNED_UP("M0001", HttpStatus.CONFLICT, "이미 회원가입한 회원입니다"),
    MEMBER_NOT_FOUND("M0002", HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다. 다시 로그인해 주세요"),
    ALREADY_WITHDRAWN("M0003", HttpStatus.CONFLICT, "이미 탈퇴한 회원입니다"),
    ALREADY_RECOVERED("M0004", HttpStatus.CONFLICT, "이미 복구된 회원입니다"),
    SIGNUP_RESTRICTED("M0005", HttpStatus.FORBIDDEN, "탈퇴 후 2개월이 지나야 다시 가입할 수 있습니다"),
    RECOVERY_GRACE_EXPIRED("M0006", HttpStatus.BAD_REQUEST, "탈퇴 후 7일이 지나 복구할 수 없습니다"),
    RECOVERY_CONFLICT("M0007", HttpStatus.CONFLICT, "복구에 실패했습니다. 잠시 후 다시 시도해 주세요");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
