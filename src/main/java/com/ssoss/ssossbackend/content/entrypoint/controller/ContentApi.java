package com.ssoss.ssossbackend.content.entrypoint.controller;

import com.ssoss.ssossbackend.content.entrypoint.request.ContentSaveRequest;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentSaveResponse;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;

@Tag(name = "콘텐츠")
interface ContentApi {

    @Operation(
        summary = "저장하기",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            생성 작업의 결과를 콘텐츠로 저장합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 본인의 작업만 저장할 수 있습니다.
            - 저장 단위는 작업 하나입니다. 작업 id 를 보내면 그 작업의 채널 결과가 전부 한 번에 저장됩니다.
              채널을 골라 저장하는 방법은 없습니다.
            - 성공한 작업만 저장할 수 있습니다. 조회 API 의 status 가 SUCCEEDED 인 작업을 보내세요.
              아직 IN_PROGRESS 면 409 로, FAILED 면 400 으로 거부됩니다.
            - 같은 작업을 다시 저장해도 콘텐츠가 늘어나지 않습니다. 이미 저장된 결과는 그대로 두고 같은 콘텐츠 id 를 반환합니다.
            - 반환된 contentId 로 생성 기록의 상세 조회·편집·채널 변환에 들어갑니다.
            - contents 는 블로그 → 인스타그램 → 당근 비즈 → 스레드 순으로 담깁니다. 요청한 채널 순서나 저장된 순서와 무관하게 항상 같습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "저장되었습니다. 이미 저장된 작업이면 기존 콘텐츠를 그대로 반환합니다",
            content = @Content(schema = @Schema(implementation = ContentSaveResponse.class),
                examples = @ExampleObject(name = "2채널 저장", value = """
                    {
                      "contents": [
                        {"contentId": 1, "generationResultId": 10, "channel": "BLOG"},
                        {"contentId": 2, "generationResultId": 11, "channel": "INSTAGRAM"}
                      ]
                    }
                    """))),
        @ApiResponse(responseCode = "400",
            description = "입력값이 잘못되었거나 (C0001) 생성에 실패한 작업입니다 (CT0003)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "입력값 오류", value = """
                        {"code":"C0001","message":"생성 작업 id 를 입력해 주세요"}
                        """),
                    @ExampleObject(name = "실패한 작업", value = """
                        {"code":"CT0003","message":"생성에 실패한 작업은 저장할 수 없습니다"}
                        """)})),
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
        @ApiResponse(responseCode = "404", description = "작업이 없거나 본인의 작업이 아닙니다 (CT0002)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"CT0002","message":"생성 작업을 찾을 수 없습니다"}
                    """))),
        @ApiResponse(responseCode = "409", description = "작업이 아직 진행 중입니다 (CT0004) — 조회 API 의 status 가 IN_PROGRESS 가 아니게 된 뒤에 저장해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"CT0004","message":"생성이 아직 끝나지 않았습니다. 끝난 뒤 저장해 주세요"}
                    """)))
    })
    ResponseEntity<ContentSaveResponse> save(Long memberId, ContentSaveRequest request);
}
