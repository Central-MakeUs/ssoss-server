package com.ssoss.ssossbackend.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

@TestConfiguration(proxyBeanMethods = false)
class TestNaverApiConfig {

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    TestNaverApi testNaverApi() {
        return new TestNaverApi();
    }

    @Bean
    DynamicPropertyRegistrar naverOAuthPropertiesRegistrar(TestNaverApi testNaverApi) {
        return registry -> {
            registry.add("auth.oauth.naver.profile-url", testNaverApi::profileUrl);
            registry.add("auth.oauth.naver.revoke-url", testNaverApi::revokeUrl);
            registry.add("auth.oauth.naver.client-id", () -> "test-naver-client-id");
            registry.add("auth.oauth.naver.client-secret", () -> "test-naver-client-secret");
        };
    }
}
