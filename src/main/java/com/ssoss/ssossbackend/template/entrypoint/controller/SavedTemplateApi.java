package com.ssoss.ssossbackend.template.entrypoint.controller;

import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.template.entrypoint.request.SavedTemplateSaveRequest;
import com.ssoss.ssossbackend.template.entrypoint.response.SavedTemplateSaveResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;

@Tag(name = "추천 템플릿")
interface SavedTemplateApi {

    @Operation(
        summary = "템플릿 저장하기",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            편집 화면에서 다듬은 본문을 회원의 글로 저장합니다. 저장하기를 눌렀을 때 부르는 API 입니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 저장한 글은 부른 회원의 것이 됩니다.
            - 적용(`GET /v1/templates/{templateId}/applied`)은 아무것도 남기지 않으므로, 저장은 이 API 로만 이뤄집니다.
            - body 는 화면에 보이는 최종 본문을 그대로 보냅니다. 서버가 자리표시자를 다시 채우거나 원본 본문으로 덮어쓰지 않습니다.
            - 제목·설명·분류·추천 채널은 저장 시점에 원본 템플릿에서 복사합니다.
              운영자가 나중에 원본을 고쳐도 저장한 글은 그대로입니다. 예시 본문은 복사하지 않습니다.
            - 같은 템플릿을 여러 번 저장하면 저장한 만큼 각각 남습니다. 같은 템플릿으로 이번 달과 다음 달에 서로 다른 글을 쓰기 위해서입니다.
            - body 는 2000자까지이고, 비어 있거나 공백만 있으면 400(C0001)입니다.
            - AI 를 거치지 않으므로 크레딧이 차감되지 않습니다.
            - 응답의 savedTemplateId 는 저장한 글의 id 라 요청에 보낸 templateId 와 다른 값입니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "저장한 글의 id 를 반환합니다",
            content = @Content(schema = @Schema(implementation = SavedTemplateSaveResponse.class),
                examples = @ExampleObject(value = """
                    {"savedTemplateId": 1}
                    """))),
        @ApiResponse(responseCode = "400", description = "templateId 가 없거나 body 가 비었거나 2000자를 넘었습니다 (C0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"C0001","message":"본문은 2000자 이내로 입력해 주세요"}
                    """))),
        @ApiResponse(responseCode = "401", description = "accessToken 이 없거나 유효하지 않습니다 (A0006) — 다시 로그인해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"A0006","message":"유효하지 않은 인증 정보입니다. 다시 로그인해 주세요"}
                    """))),
        @ApiResponse(responseCode = "403", description = "가입 회원(ACTIVE) 토큰이 아닙니다 (A0007) — 가입 대기·탈퇴 대기 상태에서는 호출할 수 없습니다",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"A0007","message":"접근 권한이 없습니다"}
                    """))),
        @ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없습니다 (TP0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"TP0001","message":"템플릿을 찾을 수 없습니다"}
                    """)))
    })
    ResponseEntity<SavedTemplateSaveResponse> save(Long memberId, SavedTemplateSaveRequest request);
}
