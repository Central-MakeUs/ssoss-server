package com.ssoss.ssossbackend.template.entrypoint.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 매장 정보로 자리표시자를 채운 추천 템플릿 본문")
public record TemplateAppliedResponse(
    @Schema(description = "적용한 템플릿 id — 요청한 템플릿과 같은 값입니다", example = "8")
    Long id,
    @Schema(description = "채울 수 있는 자리표시자를 바꾼 본문 — 채울 값이 없으면 자리표시자가 그대로 남습니다",
        example = "보니스커피에 새 메뉴가 출시되었습니다!\n\n🎁신메뉴: [메뉴명]\n\n📍서울 중구 을지로 100")
    String body
) {
}
