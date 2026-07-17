package com.ssoss.ssossbackend.auth.infrastructure.security;

import com.ssoss.ssossbackend.auth.domain.model.MemberStatus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
class SecurityConfig {

    private static final String[] DOCS_PATHS = {"/scalar", "/scalar/**", "/v3/api-docs", "/v3/api-docs/**"};

    @Bean
    WebSecurityCustomizer docsSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(DOCS_PATHS);
    }

    @Bean
    SecurityFilterChain apiSecurityFilterChain(
        HttpSecurity http,
        AccessTokenAuthenticator accessTokenAuthenticator,
        ErrorResponseAuthenticationEntryPoint authenticationEntryPoint,
        ErrorResponseAccessDeniedHandler accessDeniedHandler
    ) {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/v1/social-logins/*").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/tokens").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/logout").permitAll()
                .anyRequest().hasAuthority(MemberStatus.ACTIVE.name()))
            .addFilterBefore(new JwtAuthenticationFilter(accessTokenAuthenticator), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }
}
