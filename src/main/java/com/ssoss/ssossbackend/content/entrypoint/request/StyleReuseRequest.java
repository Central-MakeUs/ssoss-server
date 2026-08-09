package com.ssoss.ssossbackend.content.entrypoint.request;

import java.util.List;

import com.ssoss.ssossbackend.content.application.command.StyleReuseCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "스타일 재사용 요청")
public record StyleReuseRequest(
    @Schema(description = "강조 내용 (필수) — 새 글의 소재는 여기에서만 나옵니다",
        example = "이번 주 새로 나온 파니니", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "강조 내용을 입력해 주세요")
    @Size(max = 500, message = "강조 내용은 500자 이내로 입력해 주세요")
    String emphasis,
    @Schema(description = "금지 내용 (선택) — 콘텐츠에 나오면 안 되는 내용", example = "가격 인상 언급",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 500, message = "금지 내용은 500자 이내로 입력해 주세요")
    String forbidden,
    @Schema(description = "키워드 목록 (선택) — 본문에 녹일 단어들, 최대 10개",
        example = "[\"디저트 맛집\", \"데이트 코스\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 10, message = "키워드는 최대 10개까지 입력할 수 있습니다")
    List<@NotBlank(message = "빈 키워드는 보낼 수 없습니다")
        @Size(max = 30, message = "키워드는 30자 이내로 입력해 주세요") String> keywords,
    @Schema(description = "사진 가이드 체크 여부 (선택) — 체크하면 본문에 사진 안내 태그가 함께 담깁니다",
        example = "true", defaultValue = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Boolean photoGuideChecked
) {

    public StyleReuseCommand toCommand(Long memberId, Long contentId, Long contentChannelId) {
        return new StyleReuseCommand(memberId, contentId, contentChannelId, emphasis, forbidden, keywords,
            Boolean.TRUE.equals(photoGuideChecked));
    }
}
