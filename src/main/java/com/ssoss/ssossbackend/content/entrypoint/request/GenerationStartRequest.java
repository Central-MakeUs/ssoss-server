package com.ssoss.ssossbackend.content.entrypoint.request;

import java.util.List;

import com.ssoss.ssossbackend.content.application.command.GenerationStartCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "생성 작업 생성 요청")
public record GenerationStartRequest(
    @Schema(description = "콘텐츠를 만들 채널 목록 (1~4개, 중복 불가)",
        allowableValues = {"BLOG", "INSTAGRAM", "DAANGN_BIZ", "THREADS"}, example = "[\"BLOG\", \"INSTAGRAM\"]",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "채널을 1개 이상 선택해 주세요")
    @Size(max = 4, message = "채널은 최대 4개까지 선택할 수 있습니다")
    List<String> channels,
    @Schema(description = "목적 — INFORMATION: 정보성, EVENT_DISCOUNT: 이벤트/할인, NEW_MENU_PROMOTION: 신메뉴/홍보",
        allowableValues = {"INFORMATION", "EVENT_DISCOUNT", "NEW_MENU_PROMOTION"}, example = "INFORMATION",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "목적을 선택해 주세요")
    String purpose,
    @Schema(description = "톤 — CASUAL: 일상형, EMOTIONAL: 감성형, INFORMATIVE: 정보형, PROMOTIONAL: 홍보형",
        allowableValues = {"CASUAL", "EMOTIONAL", "INFORMATIVE", "PROMOTIONAL"}, example = "CASUAL",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "톤을 선택해 주세요")
    String tone,
    @Schema(description = "강조 내용 (필수)", example = "이번 주말 아메리카노 1+1 이벤트",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "강조 내용을 입력해 주세요")
    @Size(max = 500, message = "강조 내용은 500자 이내로 입력해 주세요")
    String emphasis,
    @Schema(description = "금지 내용 (선택) — 콘텐츠에 나오면 안 되는 내용", example = "가격 인상 언급",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 500, message = "금지 내용은 500자 이내로 입력해 주세요")
    String forbidden,
    @Schema(description = "키워드 (선택) — 본문에 녹일 단어들", example = "디저트 맛집, 데이트 코스",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 500, message = "키워드는 500자 이내로 입력해 주세요")
    String keywords
) {

    public GenerationStartCommand toCommand(Long memberId) {
        return GenerationStartCommand.of(memberId, channels, purpose, tone, emphasis, forbidden, keywords);
    }
}
