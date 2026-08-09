package com.ssoss.ssossbackend.content.entrypoint.request;

import com.ssoss.ssossbackend.content.application.command.ContentRenameCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "콘텐츠 이름 수정 요청")
public record ContentRenameRequest(
    @Schema(description = "고친 이름", example = "9월 신메뉴 안내", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이름을 입력해 주세요")
    @Size(max = 20, message = "이름은 20자 이내로 입력해 주세요")
    String name
) {

    public ContentRenameCommand toCommand(Long memberId, Long contentId) {
        return new ContentRenameCommand(memberId, contentId, name);
    }
}
