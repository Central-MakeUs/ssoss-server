package com.ssoss.ssossbackend.auth.infrastructure.oauth;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;

interface NaverProfileHttpClient {

    @GetExchange
    NaverProfileResponse fetchProfile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);
}
