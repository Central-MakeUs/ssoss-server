package com.ssoss.ssossbackend.template.entrypoint.request;

import com.ssoss.ssossbackend.template.application.command.SavedTemplateEditCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "저장한 템플릿 편집 요청")
public record SavedTemplateEditRequest(
    @Schema(description = "고친 제목", example = "9월 신메뉴 안내", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "제목을 입력해 주세요")
    @Size(max = 100, message = "제목은 100자 이내로 입력해 주세요")
    String title,
    @Schema(description = "고친 본문", example = "보니스커피에 새 메뉴가 출시되었습니다!\n\n🎁신메뉴: 흑임자 라떼",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "본문을 입력해 주세요")
    @Size(max = 2000, message = "본문은 2000자 이내로 입력해 주세요")
    String body
) {

    public SavedTemplateEditCommand toCommand(Long memberId, Long savedTemplateId) {
        return new SavedTemplateEditCommand(memberId, savedTemplateId, title, body);
    }
}
