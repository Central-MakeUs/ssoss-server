package com.ssoss.ssossbackend.documentation;

import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI ssossOpenApi() {
        return new OpenAPI()
            .info(new Info().title("SSOSS API").version("v1"))
            .tags(List.of(new Tag()
                .name("인증")
                .description("""
                    소셜 프로바이더 인증으로 세션을 시작하고, 발급된 토큰 쌍으로 세션을 유지·종료하는 API 그룹입니다.

                    **토큰 모델**
                    - 액세스 토큰 — JWT. `Authorization: Bearer {accessToken}` 헤더로 API 를 호출합니다.
                    - 리프레시 토큰 — opaque 문자열. 토큰 재발급·로그아웃 요청 본문으로만 제출합니다.

                    **세션 생명주기**
                    1. 소셜 로그인 — 프로바이더 액세스 토큰을 검증하고 서버 자체 토큰 쌍을 발급합니다.
                    2. 토큰 재발급 — 리프레시 토큰을 회전(RTR)해 새 토큰 쌍을 발급합니다. 만료 시각은 회전마다 다시 연장됩니다.
                    3. 로그아웃 — 현재 세션의 리프레시 토큰을 폐기합니다. 다른 기기의 세션은 유지됩니다.
                    """)));
    }
}
