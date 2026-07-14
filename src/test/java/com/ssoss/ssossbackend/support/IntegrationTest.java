package com.ssoss.ssossbackend.support;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "scheduling.enabled=false")
@Import({TestcontainersConfiguration.class, TestSecurityConfig.class, TestFixtureConfig.class, TestNaverApiConfig.class,
    TestAppleApiConfig.class, TestClockConfig.class})
public abstract class IntegrationTest {

    @Autowired
    protected TestFixture fixture;

    @Autowired
    protected TestNaverApi naverApi;

    @Autowired
    protected TestAppleApi appleApi;

    @Autowired
    protected MutableClock clock;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void resetClock() {
        clock.reset();
    }

    @BeforeEach
    void resetDatabase() {
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    protected RestTestClient client() {
        return fixture.client();
    }
}
