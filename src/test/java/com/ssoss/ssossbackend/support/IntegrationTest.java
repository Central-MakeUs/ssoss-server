package com.ssoss.ssossbackend.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "scheduling.enabled=false")
@Import({TestcontainersConfiguration.class, TestSecurityConfig.class, TestFixtureConfig.class, TestNaverApiConfig.class,
    TestAppleApiConfig.class, TestClockConfig.class, FailingMemberDeletedListenerConfig.class, TestLlmApiConfig.class,
    TestTaskExecutorConfig.class})
public abstract class IntegrationTest {

    @Autowired
    protected TestFixture fixture;

    @Autowired
    protected TestNaverApi naverApi;

    @Autowired
    protected TestAppleApi appleApi;

    @Autowired
    protected TestLlmApi llmApi;

    @Autowired
    protected TestTaskExecutor taskExecutor;

    @Autowired
    protected MutableClock clock;

    @Autowired
    protected JwtTestSupport jwtTestSupport;

    @Autowired
    protected FailingMemberDeletedListener failingMemberDeletedListener;

    @BeforeEach
    void resetClock() {
        clock.reset();
    }

    @BeforeEach
    void resetFailingMemberDeletedListener() {
        failingMemberDeletedListener.reset();
    }

    @BeforeEach
    void resetLlmApi() {
        llmApi.reset();
    }

    @BeforeEach
    void resetTaskExecutor() {
        taskExecutor.reset();
    }

    protected RestTestClient client() {
        return fixture.client();
    }
}
