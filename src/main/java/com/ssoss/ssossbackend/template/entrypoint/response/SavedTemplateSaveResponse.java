package com.ssoss.ssossbackend.template.entrypoint.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "템플릿 저장하기 응답")
public record SavedTemplateSaveResponse(
    @Schema(description = "저장한 템플릿 id — 원본 템플릿 id 와 다릅니다", example = "1")
    Long savedTemplateId
) {
}
