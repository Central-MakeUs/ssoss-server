package com.ssoss.ssossbackend.auth.infrastructure.oauth;

import org.springframework.web.service.annotation.GetExchange;

interface AppleJwksHttpClient {

    @GetExchange
    String fetchKeys();
}
