package com.ssoss.ssossbackend.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.test.web.servlet.client.RestTestClient;

@TestConfiguration(proxyBeanMethods = false)
class TestFixtureConfig {

    @Bean
    @Scope("prototype")
    TestFixture testFixture(@LocalServerPort int port, TestNaverApi naverApi) {
        RestTestClient client = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        return new TestFixture(client, naverApi);
    }

    @Bean
    JwtTestSupport jwtTestSupport(@Value("${auth.jwt.secret}") String secret) {
        return new JwtTestSupport(secret);
    }
}
