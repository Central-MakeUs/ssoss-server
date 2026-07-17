package com.ssoss.ssossbackend.auth.entrypoint.controller;

import com.ssoss.ssossbackend.shared.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "인증")
interface WithdrawalApi {

    @Operation(
        summary = "탈퇴",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            가입 회원(ACTIVE)이 자신의 회원 리소스 삭제(탈퇴)를 요청해 탈퇴 대기(WITHDRAWN)로 전환합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 입니다. 요청 본문은 없습니다.
            - 탈퇴하면 모든 기기의 리프레시 토큰이 무효화되어 토큰 재발급이 불가합니다.
              액세스 토큰은 즉시 무효화되지 않고 자체 만료 시각까지 유효합니다.
            - 탈퇴 대기 7일 동안은 회원 데이터가 그대로 보존되는 복구 유예 기간이며, 7일이 지나면 회원 데이터는 삭제 대상이 됩니다.
            - 탈퇴 대기 중 로그인하면 role 이 WITHDRAWN 인 토큰이 발급됩니다. 복구 용도 전용이라 그 외 API 호출은 403 을 응답합니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "탈퇴 처리되어 탈퇴 대기(WITHDRAWN)로 전환되었습니다"),
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
        @ApiResponse(responseCode = "409", description = "이미 탈퇴한 회원입니다 (M0003)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"M0003","message":"이미 탈퇴한 회원입니다"}
                    """)))
    })
    void withdraw(Long memberId);
}
