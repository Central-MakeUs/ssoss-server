package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import com.ssoss.ssossbackend.content.domain.contract.ContentGenerator;
import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.GenerationMaterial;
import com.ssoss.ssossbackend.content.domain.model.LlmCallReply;
import com.ssoss.ssossbackend.content.domain.model.Purpose;
import com.ssoss.ssossbackend.content.domain.model.StoreMaterial;
import com.ssoss.ssossbackend.content.domain.model.Tone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("live")
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Gemini 실제 배선 라이브")
class GeminiAutoConfiguredClientLiveTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class Containers {

        @Bean
        @ServiceConnection
        MySQLContainer mysqlContainer() {
            return new MySQLContainer(DockerImageName.parse("mysql:latest"));
        }
    }

    @Autowired
    private ContentGenerator contentGenerator;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("auth.oauth.apple.private-key", GeminiAutoConfiguredClientLiveTest::throwawayEcPrivateKey);
    }

    @Test
    @DisplayName("운영과 같은 애플리케이션 컨텍스트로 실호출하면 콘텐츠가 생성된다")
    void generatesContent_throughApplicationWiredClient() {
        LlmCallReply reply = contentGenerator.generate(new GenerationMaterial(
            Channel.BLOG, Purpose.NEW_MENU_PROMOTION, Tone.CASUAL,
            "가을 신메뉴 밤라떼 출시", "가격 인상 언급", List.of("동네 카페"), false,
            new StoreMaterial("보니스커피", "카페", "서울 중구 을지로 100", "을지로 크루아상 카페",
                List.of("월요일", "화요일"), "09:00", "22:00", List.of("크루아상"), List.of("포장 가능"))));

        assertThat(reply.content().hasRequiredOutput(Channel.BLOG)).isTrue();
        assertThat(reply.outputTokens()).isPositive();
    }

    private static String throwawayEcPrivateKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(256);
            return Base64.getEncoder().encodeToString(generator.generateKeyPair().getPrivate().getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
