package com.ssoss.ssossbackend.support;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationStartResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

public class TestFixture {

    private final RestTestClient client;
    private final TestNaverApi naverApi;

    TestFixture(RestTestClient client, TestNaverApi naverApi) {
        this.client = client;
        this.naverApi = naverApi;
    }

    public RestTestClient client() {
        return client;
    }

    public RestTestClient.ResponseSpec naverLogin(String socialId) {
        naverApi.stubProfile(socialId + "-token", socialId);
        return socialLogin(SocialProvider.NAVER, socialId + "-token");
    }

    public RestTestClient.ResponseSpec socialLogin(SocialProvider provider, String accessToken) {
        return client.post().uri("/v1/social-logins/" + provider.name().toLowerCase(Locale.ROOT))
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("accessToken", accessToken))
            .exchange();
    }

    public RestTestClient.ResponseSpec signup(String accessToken) {
        return signup(accessToken, true, true, true);
    }

    public RestTestClient.ResponseSpec signup(String accessToken, boolean serviceTermsAgreed,
        boolean privacyPolicyAgreed, boolean marketingAgreed) {
        return client.post().uri("/v1/signup")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("serviceTermsAgreed", serviceTermsAgreed, "privacyPolicyAgreed", privacyPolicyAgreed,
                "marketingAgreed", marketingAgreed))
            .exchange();
    }

    public RestTestClient.ResponseSpec withdraw(String accessToken) {
        return client.delete().uri("/v1/members/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public SignupResponse signupActiveMember(String socialId) {
        SocialLoginResponse login = naverLoginMember(socialId);
        return signup(login.accessToken())
            .expectStatus().isOk()
            .expectBody(SignupResponse.class)
            .returnResult()
            .getResponseBody();
    }

    public SocialLoginResponse naverLoginMember(String socialId) {
        return naverLogin(socialId)
            .expectStatus().isOk()
            .expectBody(SocialLoginResponse.class)
            .returnResult()
            .getResponseBody();
    }

    public RestTestClient.ResponseSpec recover(String accessToken) {
        return client.post().uri("/v1/members/me/recovery")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public RestTestClient.ResponseSpec creditBalance(String accessToken) {
        return client.get().uri("/v1/credits/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public RestTestClient.ResponseSpec startGeneration(String accessToken, List<String> channels) {
        return startGeneration(accessToken, Map.of(
            "channels", channels,
            "purpose", "INFORMATION",
            "tone", "CASUAL",
            "emphasis", "테스트 강조 내용"));
    }

    public RestTestClient.ResponseSpec startGeneration(String accessToken, Map<String, Object> body) {
        return client.post().uri("/v1/generations")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange();
    }

    public Long startedGenerationId(String accessToken, List<String> channels) {
        return startGeneration(accessToken, channels)
            .expectStatus().isCreated()
            .expectBody(GenerationStartResponse.class)
            .returnResult()
            .getResponseBody()
            .generationId();
    }

    public RestTestClient.ResponseSpec pollGeneration(String accessToken, Long generationId) {
        return client.get().uri("/v1/generations/" + generationId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public RestTestClient.ResponseSpec refreshTokens(String refreshToken) {
        return client.post().uri("/v1/tokens")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("refreshToken", refreshToken))
            .exchange();
    }

    public RestTestClient.ResponseSpec logout(String refreshToken) {
        return client.post().uri("/v1/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("refreshToken", refreshToken))
            .exchange();
    }
}
