package com.ssoss.ssossbackend.auth.entrypoint.controller;

import com.ssoss.ssossbackend.auth.entrypoint.response.RecoveryResponse;
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
interface RecoveryApi {

    @Operation(
        summary = "복구",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            탈퇴 대기(WITHDRAWN) 회원이 7일 복구 유예 안에 가입 회원(ACTIVE)으로 되살아납니다.

            - 탈퇴 대기 로그인에서 받은 WITHDRAWN accessToken 전용 API 입니다. 요청 본문은 없습니다.
            - 복구되면 가입 회원(ACTIVE)으로 복원되고 role=ACTIVE 정식 토큰 쌍(access + refresh)이 새로 발급됩니다.
              이후 API 는 새 accessToken 으로 호출합니다.
            - 회원 데이터는 삭제 전이므로 그대로 유지됩니다.
            - 탈퇴 전에 발급된 리프레시 토큰은 복구 후에도 계속 무효입니다. 복구 이후 발급된 토큰만 유효합니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "복구에 성공해 가입 회원 상태와 새 토큰 쌍을 반환합니다",
            content = @Content(schema = @Schema(implementation = RecoveryResponse.class),
                examples = @ExampleObject(value = """
                    {"status":"ACTIVE","accessToken":"eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxIn0.x","refreshToken":"3q2nq0uW9kZ0m1r5c8vX2yB7dF4hJ6lN8pR0tV2xZ4A"}
                    """))),
        @ApiResponse(responseCode = "401", description = "accessToken 이 없거나 유효하지 않습니다 (A0006) — 다시 로그인해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"A0006","message":"유효하지 않은 인증 정보입니다. 다시 로그인해 주세요"}
                    """))),
        @ApiResponse(responseCode = "403", description = "탈퇴 대기(WITHDRAWN) 토큰이 아닙니다 (A0007) — 탈퇴 대기 상태에서만 호출할 수 있습니다",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"A0007","message":"접근 권한이 없습니다"}
                    """))),
        @ApiResponse(responseCode = "409", description = "이미 복구된 회원입니다 (M0004) — 다시 로그인해 ACTIVE 토큰을 발급받아 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"M0004","message":"이미 복구된 회원입니다"}
                    """)))
    })
    RecoveryResponse recover(Long memberId);
}
