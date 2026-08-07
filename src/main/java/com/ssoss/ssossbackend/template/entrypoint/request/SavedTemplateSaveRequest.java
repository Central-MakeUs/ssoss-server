package com.ssoss.ssossbackend.template.entrypoint.request;

import com.ssoss.ssossbackend.template.application.command.SavedTemplateSaveCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "템플릿 저장하기 요청")
public record SavedTemplateSaveRequest(
    @Schema(description = "저장할 원본 템플릿 id", example = "8", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "템플릿 id 를 입력해 주세요")
    Long templateId,
    @Schema(description = "편집 화면에서 고친 최종 본문", example = "보니스커피에 새 메뉴가 출시되었습니다!\n\n🎁신메뉴: 흑임자 라떼",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "본문을 입력해 주세요")
    @Size(max = 2000, message = "본문은 2000자 이내로 입력해 주세요")
    String body
) {

    public SavedTemplateSaveCommand toCommand(Long memberId) {
        return new SavedTemplateSaveCommand(memberId, templateId, body);
    }
}
