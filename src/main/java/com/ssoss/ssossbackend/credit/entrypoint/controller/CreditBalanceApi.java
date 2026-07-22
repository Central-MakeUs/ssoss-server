package com.ssoss.ssossbackend.credit.entrypoint.controller;

import com.ssoss.ssossbackend.credit.entrypoint.response.CreditBalanceResponse;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "크레딧")
interface CreditBalanceApi {

    @Operation(
        summary = "잔여 크레딧 조회",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            가입 회원(ACTIVE)이 당사이클 잔여 크레딧과 사이클 한도를 조회합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 입니다.
            - 크레딧 사이클은 매달 3일 00:00 KST 에 시작해 다음 달 3일 직전에 끝나며, 사이클마다 한도(50)가 이월 없이 주어집니다.
            - 잔여는 사이클 한도에 당사이클 차감 기록을 합산해 파생됩니다. 가입 직후에는 잔여가 한도와 같습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "당사이클 잔여 크레딧과 사이클 한도를 반환합니다"),
        @ApiResponse(responseCode = "401", description = "accessToken 이 없거나 유효하지 않습니다 (A0006) — 다시 로그인해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"A0006","message":"유효하지 않은 인증 정보입니다. 다시 로그인해 주세요"}
                    """))),
        @ApiResponse(responseCode = "403", description = "가입 회원(ACTIVE) 토큰이 아닙니다 (A0007) — 가입 대기·탈퇴 대기 상태에서는 호출할 수 없습니다",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"A0007","message":"접근 권한이 없습니다"}
                    """)))
    })
    CreditBalanceResponse readBalance(Long memberId);
}
