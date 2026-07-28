package com.ssoss.ssossbackend.store.entrypoint.listener;

import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.store.domain.contract.StoreRepository;
import com.ssoss.ssossbackend.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.NAVER;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("매장 생성 리스너")
class StoreMemberActivatedListenerTest extends IntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Nested
    @DisplayName("회원가입하면")
    class WhenMemberSignsUp {

        @Test
        @DisplayName("그 회원의 매장 행이 모든 정보가 빈 채로 생긴다")
        void createsEmptyStore_whenMemberSignsUp() {
            fixture.signupActiveMember("naver-store-created");

            Long memberId = memberIdOf("naver-store-created");

            assertThat(storeRepository.findByMemberId(memberId))
                .get()
                .satisfies(store -> {
                    assertThat(store.getName()).isNull();
                    assertThat(store.getType()).isNull();
                    assertThat(store.getAddress()).isNull();
                    assertThat(store.getIntroduction()).isNull();
                    assertThat(store.getBusinessDays()).isNull();
                    assertThat(store.getOpenTime()).isNull();
                    assertThat(store.getCloseTime()).isNull();
                    assertThat(store.getSignatureMenus()).isNull();
                    assertThat(store.isTakeoutAvailable()).isFalse();
                    assertThat(store.isReservationAvailable()).isFalse();
                    assertThat(store.isParkingAvailable()).isFalse();
                    assertThat(store.getStrength()).isNull();
                    assertThat(store.getKeywords()).isNull();
                    assertThat(store.getForbidden()).isNull();
                    assertThat(store.getTone()).isNull();
                });
        }
    }

    @Nested
    @DisplayName("가입하지 않은 가입 대기 회원은")
    class WhenMemberStaysPending {

        @Test
        @DisplayName("매장 행을 갖지 않는다")
        void hasNoStore_whenMemberOnlyLoggedIn() {
            fixture.naverLoginMember("naver-store-pending");

            Long memberId = memberIdOf("naver-store-pending");

            assertThat(storeRepository.findByMemberId(memberId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("탈퇴 대기 회원이 복구하면")
    class WhenWithdrawnMemberRecovers {

        @Test
        @DisplayName("가입 때 만들어진 매장 행이 그대로 남는다")
        void keepsSameStore_whenWithdrawnMemberRecovers() {
            SignupResponse signup = fixture.signupActiveMember("naver-store-recovered");
            Long memberId = memberIdOf("naver-store-recovered");
            Long storeId = storeRepository.findByMemberId(memberId).orElseThrow().getId();
            fixture.withdraw(signup.accessToken()).expectStatus().isNoContent();
            String withdrawnAccessToken = fixture.naverLoginMember("naver-store-recovered").accessToken();

            fixture.recover(withdrawnAccessToken).expectStatus().isOk();

            assertThat(storeRepository.findByMemberId(memberId))
                .get()
                .satisfies(store -> assertThat(store.getId()).isEqualTo(storeId));
        }
    }

    private Long memberIdOf(String socialId) {
        return memberRepository.findByProviderAndSocialId(NAVER, socialId).orElseThrow().getId();
    }
}
