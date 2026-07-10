package com.ssoss.ssossbackend.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestcontainersConfiguration.class, TestSecurityConfig.class, TestFixtureConfig.class})
public abstract class IntegrationTest {

    @Autowired
    protected TestFixture fixture;

    protected RestTestClient client() {
        return fixture.client();
    }
}
