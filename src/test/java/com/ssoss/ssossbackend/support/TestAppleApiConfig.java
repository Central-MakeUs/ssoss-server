package com.ssoss.ssossbackend.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

@TestConfiguration(proxyBeanMethods = false)
class TestAppleApiConfig {

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    TestAppleApi testAppleApi() {
        return new TestAppleApi();
    }

    @Bean
    DynamicPropertyRegistrar appleOAuthPropertiesRegistrar(TestAppleApi testAppleApi) {
        return registry -> {
            registry.add("auth.oauth.apple.jwks-url", testAppleApi::jwksUrl);
            registry.add("auth.oauth.apple.client-id", () -> TestAppleApi.CLIENT_ID);
            registry.add("auth.oauth.apple.token-url", testAppleApi::tokenUrl);
            registry.add("auth.oauth.apple.revoke-url", testAppleApi::revokeUrl);
            registry.add("auth.oauth.apple.team-id", () -> "test-team-id");
            registry.add("auth.oauth.apple.key-id", () -> "test-signing-key-id");
            registry.add("auth.oauth.apple.private-key", testAppleApi::clientSecretPrivateKey);
        };
    }
}
