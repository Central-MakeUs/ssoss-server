package com.ssoss.ssossbackend.content.entrypoint.controller;

import com.ssoss.ssossbackend.content.entrypoint.request.GenerationStartRequest;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationStartResponse;
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

@Tag(name = "생성 작업")
interface GenerationApi {

    @Operation(
        summary = "생성 작업 생성",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            선택한 채널별 AI 콘텐츠를 만드는 생성 작업을 만들고 작업 id 를 즉시 반환합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 입니다.
            - 콘텐츠는 비동기로 생성됩니다. 반환된 작업 id 로 폴링 API 를 호출해 채널별 결과를 확인하세요.
            - 회원당 진행 중 작업은 1건으로 제한됩니다. 진행 중 작업이 있으면 409 로 거부됩니다.
            - 작업은 생성 시각부터 60초가 지나면 더 이상 결과가 더해지지 않습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 작업이 만들어졌습니다. Location 헤더와 본문의 작업 id 로 폴링할 수 있습니다"),
        @ApiResponse(responseCode = "400", description = "입력값이 잘못되었습니다 (C0001) — 채널 0개·중복 채널·목적/톤/강조 내용 누락 등",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"C0001","message":"강조 내용을 입력해 주세요"}
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
        @ApiResponse(responseCode = "409", description = "진행 중인 생성 작업이 이미 있습니다 (CT0001) — 완료된 뒤 다시 시도해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"CT0001","message":"진행 중인 생성 작업이 있습니다. 완료된 뒤 다시 시도해 주세요"}
                    """)))
    })
    ResponseEntity<GenerationStartResponse> start(Long memberId, GenerationStartRequest request);
}
