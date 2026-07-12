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
    DynamicPropertyRegistrar naverProfileUrlRegistrar(TestNaverApi testNaverApi) {
        return registry -> registry.add("auth.oauth.naver.profile-url", testNaverApi::profileUrl);
    }
}
