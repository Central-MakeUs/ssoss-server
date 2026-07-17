package com.ssoss.ssossbackend.auth.domain.contract;

import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.auth.domain.model.MemberStatus;

public interface TokenGenerator {

    LoginToken generate(Long memberId, MemberStatus status);
}
