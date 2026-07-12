package com.ssoss.ssossbackend.auth.domain.contract;

import com.ssoss.ssossbackend.auth.domain.model.LoginToken;

public interface TokenGenerator {

    LoginToken generate(Long memberId);
}
