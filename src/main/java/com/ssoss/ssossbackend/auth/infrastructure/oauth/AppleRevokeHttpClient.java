package com.ssoss.ssossbackend.auth.infrastructure.oauth;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;

interface AppleRevokeHttpClient {

    @PostExchange(contentType = "application/x-www-form-urlencoded")
    void revoke(@RequestParam MultiValueMap<String, String> form);
}
