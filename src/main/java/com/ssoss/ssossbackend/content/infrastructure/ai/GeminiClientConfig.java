package com.ssoss.ssossbackend.content.infrastructure.ai;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import com.ssoss.ssossbackend.content.domain.model.Generation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
class GeminiClientConfig {

    private static final int CALL_TIMEOUT_MILLIS = (int) Generation.DEADLINE.minusSeconds(10).toMillis();

    @Bean
    Client googleGenAiClient(Environment environment) {
        return Client.builder()
            .apiKey(environment.getRequiredProperty("spring.ai.google.genai.api-key"))
            .httpOptions(HttpOptions.builder().timeout(CALL_TIMEOUT_MILLIS).build())
            .build();
    }
}
