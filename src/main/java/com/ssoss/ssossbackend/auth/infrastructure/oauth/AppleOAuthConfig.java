package com.ssoss.ssossbackend.auth.infrastructure.oauth;

import java.net.http.HttpClient;
import java.time.Duration;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
class AppleOAuthConfig {

    @Bean
    AppleJwksHttpClient appleJwksHttpClient(
        RestClient.Builder restClientBuilder,
        @Value("${auth.oauth.apple.jwks-url}") String jwksUrl,
        @Value("${auth.oauth.apple.connect-timeout}") Duration connectTimeout,
        @Value("${auth.oauth.apple.read-timeout}") Duration readTimeout
    ) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(connectTimeout).build()
        );
        requestFactory.setReadTimeout(readTimeout);
        RestClient restClient = restClientBuilder
            .requestFactory(requestFactory)
            .baseUrl(jwksUrl)
            .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {
                throw new BusinessException(AuthErrorCode.SOCIAL_PROVIDER_UNAVAILABLE);
            })
            .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
            .build()
            .createClient(AppleJwksHttpClient.class);
    }
}
