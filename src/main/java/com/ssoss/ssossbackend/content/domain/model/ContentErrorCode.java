package com.ssoss.ssossbackend.content.domain.model;

import com.ssoss.ssossbackend.shared.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ContentErrorCode implements ErrorCode {

    GENERATION_IN_PROGRESS_EXISTS("CT0001", HttpStatus.CONFLICT, "진행 중인 생성 작업이 있습니다. 완료된 뒤 다시 시도해 주세요"),
    GENERATION_NOT_FOUND("CT0002", HttpStatus.NOT_FOUND, "생성 작업을 찾을 수 없습니다"),
    NO_SAVABLE_GENERATION_RESULT("CT0003", HttpStatus.BAD_REQUEST, "저장할 수 있는 생성 결과가 없습니다"),
    GENERATION_NOT_COMPLETED("CT0004", HttpStatus.CONFLICT, "생성이 아직 끝나지 않았습니다. 완료된 뒤 저장해 주세요");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
