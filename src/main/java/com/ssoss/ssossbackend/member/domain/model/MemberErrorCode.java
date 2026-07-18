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
    ALREADY_RECOVERED("M0004", HttpStatus.CONFLICT, "이미 복구된 회원입니다");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
