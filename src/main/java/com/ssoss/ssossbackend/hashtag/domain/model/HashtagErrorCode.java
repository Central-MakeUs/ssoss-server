package com.ssoss.ssossbackend.hashtag.domain.model;

import com.ssoss.ssossbackend.shared.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HashtagErrorCode implements ErrorCode {

    HASHTAG_BUNDLE_NOT_FOUND("HT0001", HttpStatus.NOT_FOUND, "해시태그 묶음을 찾을 수 없습니다");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
