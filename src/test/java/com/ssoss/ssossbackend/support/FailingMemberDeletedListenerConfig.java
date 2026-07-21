package com.ssoss.ssossbackend.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
class FailingMemberDeletedListenerConfig {

    @Bean
    FailingMemberDeletedListener failingMemberDeletedListener() {
        return new FailingMemberDeletedListener();
    }
}
