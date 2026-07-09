package com.ssoss.archfixtures.naming;

import com.ssoss.ssossbackend.shared.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum MemberError implements ErrorCode {

    INSTANCE;

    @Override
    public String getCode() {
        return "M0001";
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getMessage() {
        return "회원을 찾을 수 없습니다";
    }
}
