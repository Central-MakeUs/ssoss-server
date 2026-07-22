package com.ssoss.ssossbackend.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
class TestTaskExecutorConfig {

    @Bean
    @Primary
    TestTaskExecutor testTaskExecutor() {
        return new TestTaskExecutor();
    }
}
