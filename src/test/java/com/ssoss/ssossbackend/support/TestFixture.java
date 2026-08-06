package com.ssoss.ssossbackend.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentDetailResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentListResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentSaveResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationDetailResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationStartResponse;
import com.ssoss.ssossbackend.credit.entrypoint.response.CreditLedgerListResponse;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.BookmarkedHashtagBundleListResponse;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.HashtagBundleListResponse;
import com.ssoss.ssossbackend.store.entrypoint.response.StoreInfoResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
        return socialLogin(provider, accessToken, provider.name().toLowerCase(Locale.ROOT) + "-refresh-token");
    }

    public RestTestClient.ResponseSpec socialLogin(SocialProvider provider, String accessToken, String refreshToken) {
        return client.post().uri("/v1/social-logins/" + provider.name().toLowerCase(Locale.ROOT))
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("accessToken", accessToken, "refreshToken", refreshToken))
            .exchange();
    }

    public RestTestClient.ResponseSpec signup(String accessToken) {
        return signup(accessToken, true, true, true);
    }

    public RestTestClient.ResponseSpec signup(String accessToken, boolean ageOver14Agreed,
        boolean serviceTermsAgreed, boolean privacyPolicyAgreed) {
        return client.post().uri("/v1/signup")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("ageOver14Agreed", ageOver14Agreed, "serviceTermsAgreed", serviceTermsAgreed,
                "privacyPolicyAgreed", privacyPolicyAgreed))
            .exchange();
    }

    public RestTestClient.ResponseSpec withdraw(String accessToken) {
        return client.delete().uri("/v1/members/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public RestTestClient.ResponseSpec withdraw(String accessToken, String reason, String detail) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("reason", reason);
        body.put("detail", detail);
        return client.method(HttpMethod.DELETE).uri("/v1/members/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
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

    public RestTestClient.ResponseSpec getCreditLedgers(String accessToken, String query) {
        return client.get().uri("/v1/credits/me/ledgers" + query)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public CreditLedgerListResponse creditLedgerList(String accessToken, String query) {
        return getCreditLedgers(accessToken, query)
            .expectStatus().isOk()
            .expectBody(CreditLedgerListResponse.class)
            .returnResult()
            .getResponseBody();
    }

    public RestTestClient.ResponseSpec getStoreInfo(String accessToken) {
        return client.get().uri("/v1/stores/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public StoreInfoResponse storeInfo(String accessToken) {
        return getStoreInfo(accessToken)
            .expectStatus().isOk()
            .expectBody(StoreInfoResponse.class)
            .returnResult()
            .getResponseBody();
    }

    public RestTestClient.ResponseSpec saveStoreBasicInfo(String accessToken, Map<String, Object> body) {
        return client.put().uri("/v1/stores/me/basic")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange();
    }

    public Map<String, Object> storeBasicInfoBody(String name, String type, String address, String introduction) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("type", type);
        body.put("address", address);
        body.put("introduction", introduction);
        return body;
    }

    public RestTestClient.ResponseSpec saveStoreOperationInfo(String accessToken, Map<String, Object> body) {
        return client.put().uri("/v1/stores/me/operation")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange();
    }

    public Map<String, Object> storeOperationInfoBody(List<String> businessDays, String openTime, String closeTime,
        List<String> signatureMenus, Boolean takeoutAvailable, Boolean reservationAvailable,
        Boolean parkingAvailable) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("businessDays", businessDays);
        body.put("openTime", openTime);
        body.put("closeTime", closeTime);
        body.put("signatureMenus", signatureMenus);
        body.put("takeoutAvailable", takeoutAvailable);
        body.put("reservationAvailable", reservationAvailable);
        body.put("parkingAvailable", parkingAvailable);
        return body;
    }

    public RestTestClient.ResponseSpec saveStoreContentInfo(String accessToken, Map<String, Object> body) {
        return client.put().uri("/v1/stores/me/content")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange();
    }

    public Map<String, Object> storeContentInfoBody(String strength, List<String> keywords, String forbidden,
        String tone) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("strength", strength);
        body.put("keywords", keywords);
        body.put("forbidden", forbidden);
        body.put("tone", tone);
        return body;
    }

    public RestTestClient.ResponseSpec appVersion(String os, String version) {
        return client.get().uri("/v1/app-versions/{os}?version={version}", os, version)
            .exchange();
    }

    public RestTestClient.ResponseSpec startGeneration(String accessToken, List<String> channels) {
        return startGeneration(accessToken, generationBody(channels, false));
    }

    public RestTestClient.ResponseSpec startGeneration(String accessToken, Map<String, Object> body) {
        return client.post().uri("/v1/generations")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange();
    }

    public Long startedGenerationId(String accessToken, List<String> channels) {
        return startedGenerationId(accessToken, generationBody(channels, false));
    }

    public Long photoGuidedGenerationId(String accessToken, List<String> channels) {
        return startedGenerationId(accessToken, generationBody(channels, true));
    }

    private Map<String, Object> generationBody(List<String> channels, boolean photoGuideChecked) {
        return Map.of(
            "channels", channels,
            "purpose", "INFORMATION",
            "tone", "CASUAL",
            "emphasis", "테스트 강조 내용",
            "photoGuideChecked", photoGuideChecked);
    }

    public Long startedGenerationId(String accessToken, Map<String, Object> body) {
        return startGeneration(accessToken, body)
            .expectStatus().isCreated()
            .expectBody(GenerationStartResponse.class)
            .returnResult()
            .getResponseBody()
            .generationId();
    }

    public RestTestClient.ResponseSpec getGeneration(String accessToken, Long generationId) {
        return client.get().uri("/v1/generations/" + generationId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public GenerationDetailResponse generationDetail(String accessToken, Long generationId) {
        return getGeneration(accessToken, generationId)
            .expectStatus().isOk()
            .expectBody(GenerationDetailResponse.class)
            .returnResult()
            .getResponseBody();
    }

    public RestTestClient.ResponseSpec saveGeneratedContents(String accessToken, Long generationId) {
        return saveContents(accessToken, Map.of(
            "generationId", generationId,
            "contents", generationDetail(accessToken, generationId).results().stream()
                .map(result -> channelContent(result.channel(), result.title(), result.body(), result.hashtags()))
                .toList()));
    }

    public RestTestClient.ResponseSpec saveContents(String accessToken, Long generationId, List<String> channels) {
        return saveContents(accessToken, Map.of(
            "generationId", generationId,
            "contents", channels.stream()
                .map(channel -> channelContent(channel, "BLOG".equals(channel) ? "보낸 제목" : null,
                    "보낸 본문", List.of("#보낸태그")))
                .toList()));
    }

    public Map<String, Object> channelContent(String channel, String title, String body, List<String> hashtags) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("channel", channel);
        content.put("title", title);
        content.put("body", body);
        content.put("hashtags", hashtags);
        return content;
    }

    public RestTestClient.ResponseSpec saveContents(String accessToken, Map<String, Object> body) {
        return client.post().uri("/v1/contents")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange();
    }

    public ContentSaveResponse contentsOfGeneration(String accessToken, Long generationId) {
        return saveGeneratedContents(accessToken, generationId)
            .expectStatus().isCreated()
            .expectBody(ContentSaveResponse.class)
            .returnResult()
            .getResponseBody();
    }

    public Long savedContentId(String accessToken, Long generationId) {
        return contentsOfGeneration(accessToken, generationId).contentId();
    }

    public RestTestClient.ResponseSpec getContents(String accessToken, String query) {
        return client.get().uri("/v1/contents" + query)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public ContentListResponse contentList(String accessToken, String query) {
        return getContents(accessToken, query)
            .expectStatus().isOk()
            .expectBody(ContentListResponse.class)
            .returnResult()
            .getResponseBody();
    }

    public RestTestClient.ResponseSpec getContent(String accessToken, Long contentId) {
        return client.get().uri("/v1/contents/" + contentId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public ContentDetailResponse contentDetail(String accessToken, Long contentId) {
        return getContent(accessToken, contentId)
            .expectStatus().isOk()
            .expectBody(ContentDetailResponse.class)
            .returnResult()
            .getResponseBody();
    }

    public RestTestClient.ResponseSpec editContentChannel(String accessToken, Long contentId, Long contentChannelId,
        Map<String, Object> body) {
        return client.put().uri("/v1/contents/" + contentId + "/channels/" + contentChannelId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange();
    }

    public RestTestClient.ResponseSpec deleteContent(String accessToken, Long contentId) {
        return client.delete().uri("/v1/contents/" + contentId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public void deletedContent(String accessToken, Long contentId) {
        deleteContent(accessToken, contentId).expectStatus().isNoContent();
    }

    public RestTestClient.ResponseSpec getHashtagBundles(String accessToken, String query, Object... uriVariables) {
        return client.get().uri("/v1/hashtag-bundles" + query, uriVariables)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public HashtagBundleListResponse hashtagBundleList(String accessToken, String query, Object... uriVariables) {
        return getHashtagBundles(accessToken, query, uriVariables)
            .expectStatus().isOk()
            .expectBody(HashtagBundleListResponse.class)
            .returnResult()
            .getResponseBody();
    }

    public RestTestClient.ResponseSpec getBookmarkedHashtagBundles(String accessToken) {
        return client.get().uri("/v1/members/me/hashtag-bundles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public BookmarkedHashtagBundleListResponse bookmarkedHashtagBundleList(String accessToken) {
        return getBookmarkedHashtagBundles(accessToken)
            .expectStatus().isOk()
            .expectBody(BookmarkedHashtagBundleListResponse.class)
            .returnResult()
            .getResponseBody();
    }

    public RestTestClient.ResponseSpec bookmarkHashtagBundle(String accessToken, Long bundleId) {
        return client.put().uri("/v1/members/me/hashtag-bundles/" + bundleId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public void bookmarkedHashtagBundle(String accessToken, Long bundleId) {
        bookmarkHashtagBundle(accessToken, bundleId).expectStatus().isNoContent();
    }

    public RestTestClient.ResponseSpec unbookmarkHashtagBundle(String accessToken, Long bundleId) {
        return client.delete().uri("/v1/members/me/hashtag-bundles/" + bundleId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }

    public void unbookmarkedHashtagBundle(String accessToken, Long bundleId) {
        unbookmarkHashtagBundle(accessToken, bundleId).expectStatus().isNoContent();
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
