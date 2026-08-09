package com.ssoss.ssossbackend.template.entrypoint.request;

import com.ssoss.ssossbackend.template.application.command.SavedTemplateRenameCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "저장한 템플릿 제목 수정 요청")
public record SavedTemplateRenameRequest(
    @Schema(description = "고친 제목", example = "9월 신메뉴 안내", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "제목을 입력해 주세요")
    @Size(max = 100, message = "제목은 100자 이내로 입력해 주세요")
    String title
) {

    public SavedTemplateRenameCommand toCommand(Long memberId, Long savedTemplateId) {
        return new SavedTemplateRenameCommand(memberId, savedTemplateId, title);
    }
}
