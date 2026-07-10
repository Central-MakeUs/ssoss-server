package com.ssoss.ssossbackend.support;

import org.springframework.test.web.servlet.client.RestTestClient;

public class TestFixture {

    private final RestTestClient client;

    TestFixture(RestTestClient client) {
        this.client = client;
    }

    public RestTestClient client() {
        return client;
    }
}
