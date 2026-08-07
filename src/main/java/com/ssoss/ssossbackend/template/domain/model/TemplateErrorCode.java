package com.ssoss.ssossbackend.template.domain.model;

import com.ssoss.ssossbackend.shared.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TemplateErrorCode implements ErrorCode {

    TEMPLATE_NOT_FOUND("TP0001", HttpStatus.NOT_FOUND, "템플릿을 찾을 수 없습니다");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
