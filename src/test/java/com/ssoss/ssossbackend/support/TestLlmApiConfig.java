package com.ssoss.ssossbackend.support;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
class TestLlmApiConfig {

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    TestLlmApi testLlmApi() {
        return new TestLlmApi();
    }

    @Bean
    @Primary
    Client testGoogleGenAiClient(TestLlmApi testLlmApi) {
        return Client.builder()
            .apiKey("test-gemini-api-key")
            .httpOptions(HttpOptions.builder().baseUrl(testLlmApi.baseUrl()).build())
            .build();
    }
}
