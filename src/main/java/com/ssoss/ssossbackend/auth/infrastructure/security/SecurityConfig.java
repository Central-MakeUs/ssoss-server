package com.ssoss.ssossbackend.auth.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
public class SecurityConfig {

    private static final String[] DOCS_PATHS = {"/scalar", "/scalar/**", "/v3/api-docs", "/v3/api-docs/**"};

    @Bean
    WebSecurityCustomizer docsSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(DOCS_PATHS);
    }
}
