package com.ssoss.ssossbackend.support;

import java.util.Locale;
import java.util.Map;

import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

public class TestFixture {

    private final RestTestClient client;

    TestFixture(RestTestClient client) {
        this.client = client;
    }

    public RestTestClient client() {
        return client;
    }

    public RestTestClient.ResponseSpec socialLogin(SocialProvider provider, String accessToken) {
        return client.post().uri("/v1/social-logins/" + provider.name().toLowerCase(Locale.ROOT))
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("accessToken", accessToken))
            .exchange();
    }
}
