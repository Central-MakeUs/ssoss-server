package com.ssoss.ssossbackend.credit.entrypoint.controller;

import java.util.List;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.credit.domain.contract.CreditLedgerRepository;
import com.ssoss.ssossbackend.credit.domain.model.CreditLedger;
import com.ssoss.ssossbackend.credit.domain.model.CreditLedgerType;
import com.ssoss.ssossbackend.credit.entrypoint.response.CreditBalanceResponse;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.NAVER;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("크레딧 잔액 조회 API")
class CreditBalanceApiTest extends IntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CreditLedgerRepository creditLedgerRepository;

    @Nested
    @DisplayName("GET /v1/credits/me")
    class Balance {

        @Test
        @DisplayName("가입 직후 조회하면 가입 지급 무료 크레딧 50 이 잔액으로 반환된다")
        void returnsSignupGrantedBalance_whenActiveMemberQueriesRightAfterSignup() {
            SignupResponse signup = fixture.signupActiveMember("naver-credit-balance");

            fixture.creditBalance(signup.accessToken())
                .expectStatus().isOk()
                .expectBody(CreditBalanceResponse.class)
                .value(body -> assertThat(body.balance()).isEqualTo(50));
        }

        @Test
        @DisplayName("가입 대기(PENDING) 토큰으로 조회하면 403 과 A0007 을 반환한다")
        void returns403_whenPendingTokenQueriesBalance() {
            SocialLoginResponse login = fixture.naverLoginMember("naver-credit-pending");

            fixture.creditBalance(login.accessToken())
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("탈퇴 대기(WITHDRAWN) 토큰으로 조회하면 403 과 A0007 을 반환한다")
        void returns403_whenWithdrawnTokenQueriesBalance() {
            SignupResponse signup = fixture.signupActiveMember("naver-credit-withdrawn");
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            SocialLoginResponse withdrawnLogin = fixture.naverLoginMember("naver-credit-withdrawn");

            fixture.creditBalance(withdrawnLogin.accessToken())
                .expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.ACCESS_DENIED.getCode()));
        }

        @Test
        @DisplayName("액세스 토큰 없이 조회하면 401 과 A0006 을 반환한다")
        void returns401_whenAccessTokenMissing() {
            fixture.client().get().uri("/v1/credits/me")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(ErrorResponse.class)
                .value(body -> assertThat(body.code()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode()));
        }
    }

    @Nested
    @DisplayName("가입 지급의 원장 기록")
    class SignupGrantLedger {

        @Test
        @DisplayName("가입하면 지급이 원장에 GRANT 행으로 기록되고 원장 합이 잔액과 같다")
        void recordsGrantLedgerEntryMatchingBalance_whenMemberSignsUp() {
            SignupResponse signup = fixture.signupActiveMember("naver-credit-ledger");

            Long memberId = memberIdOf("naver-credit-ledger");
            List<CreditLedger> entries = ledgerOf(memberId);

            assertThat(entries).singleElement().satisfies(entry -> {
                assertThat(entry.getType()).isEqualTo(CreditLedgerType.GRANT);
                assertThat(entry.getAmount()).isEqualTo(50);
                assertThat(entry.getGenerationResultId()).isNull();
            });
            int ledgerSum = entries.stream().mapToInt(CreditLedger::getAmount).sum();
            fixture.creditBalance(signup.accessToken())
                .expectBody(CreditBalanceResponse.class)
                .value(body -> assertThat(body.balance()).isEqualTo(ledgerSum));
        }
    }

    private Long memberIdOf(String socialId) {
        return memberRepository.findByProviderAndSocialId(NAVER, socialId).orElseThrow().getId();
    }

    private List<CreditLedger> ledgerOf(Long memberId) {
        return creditLedgerRepository.findAll().stream()
            .filter(entry -> entry.getMemberId().equals(memberId))
            .toList();
    }
}
