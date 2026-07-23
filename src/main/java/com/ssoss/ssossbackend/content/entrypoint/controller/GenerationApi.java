package com.ssoss.ssossbackend.content.entrypoint.controller;

import com.ssoss.ssossbackend.content.entrypoint.request.GenerationStartRequest;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationPollResponse;
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
            - 크레딧 잔액이 차감량(5) × 선택 채널 수보다 적으면 400 으로 거부됩니다. 성공한 채널 결과 1건마다 5 가 차감됩니다.
            - 작업은 생성 시각부터 60초가 지나면 더 이상 결과가 더해지지 않습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 작업이 만들어졌습니다. Location 헤더와 본문의 작업 id 로 폴링할 수 있습니다"),
        @ApiResponse(responseCode = "400",
            description = "입력값이 잘못되었거나 (C0001 — 채널 0개·중복 채널·목적/톤/강조 내용 누락 등) 크레딧이 부족합니다 (CR0002)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "입력값 오류", value = """
                        {"code":"C0001","message":"강조 내용을 입력해 주세요"}
                        """),
                    @ExampleObject(name = "크레딧 부족", value = """
                        {"code":"CR0002","message":"크레딧이 부족합니다"}
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
        @ApiResponse(responseCode = "409", description = "진행 중인 생성 작업이 이미 있습니다 (CT0001) — 완료된 뒤 다시 시도해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"CT0001","message":"진행 중인 생성 작업이 있습니다. 완료된 뒤 다시 시도해 주세요"}
                    """)))
    })
    ResponseEntity<GenerationStartResponse> start(Long memberId, GenerationStartRequest request);

    @Operation(
        summary = "생성 작업 폴링",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            생성 작업의 진행 상태와 완성된 채널별 결과(제목·본문·해시태그)를 조회합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 본인의 작업만 조회할 수 있습니다.
            - 결과는 완성된 채널부터 순차로 채워집니다. 블로그만 제목이 있고 나머지 채널은 제목이 null 입니다.
            - status 가 SUCCEEDED 또는 FAILED 가 될 때까지 주기적으로 폴링하세요. 폴링 간격과 중단 조건은 클라이언트 소관입니다.
            - FAILED 면 failureReason 으로 실패 원인 범주를 내려주며, 원인 파악이 불가하면 null 입니다.
              실패한 작업은 재시도(새 생성 요청)로 복구합니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "작업 상태와 완성된 채널별 결과를 반환합니다"),
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
                    """)))
    })
    GenerationPollResponse poll(Long memberId, Long generationId);
}
