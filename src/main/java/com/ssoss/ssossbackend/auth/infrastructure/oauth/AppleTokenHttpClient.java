package com.ssoss.ssossbackend.auth.infrastructure.oauth;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;

interface AppleTokenHttpClient {

    @PostExchange(contentType = "application/x-www-form-urlencoded")
    AppleTokenResponse exchange(@RequestParam MultiValueMap<String, String> form);
}
