package com.ssoss.ssossbackend.auth.entrypoint.controller;

import com.ssoss.ssossbackend.auth.entrypoint.request.TokenRefreshRequest;
import com.ssoss.ssossbackend.auth.entrypoint.response.TokenRefreshResponse;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "토큰 재발급", description = "리프레시 토큰 회전(RTR) 기반 토큰 재발급 API")
interface TokenRefreshApi {

    @Operation(
        summary = "토큰 재발급",
        description = """
            리프레시 토큰을 전달하면 새 토큰 쌍(access JWT + opaque refresh)을 발급합니다.

            **RTR(Refresh Token Rotation)**
            - 재발급마다 리프레시 토큰이 회전되어 기존 토큰은 즉시 무효화됩니다. 응답의 새 리프레시 토큰으로 교체 저장해야 합니다.
            - 이미 회전된(사용된) 토큰이 다시 제출되면 401 로 거부됩니다. 같은 세션의 최신 리프레시 토큰은 계속 사용할 수 있습니다.
            - 만료 시각은 회전마다 리프레시 TTL 로 다시 연장됩니다 (슬라이딩). 만료된 토큰으로 요청하면 401 로 거부되며, 세션을 더 이상 이어갈 수 없어 재로그인이 필요합니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "재발급에 성공해 회전된 새 토큰 쌍을 반환합니다",
            content = @Content(schema = @Schema(implementation = TokenRefreshResponse.class),
                examples = @ExampleObject(value = """
                    {"accessToken":"eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxIn0.x","refreshToken":"3q2nq0uW9kZ0m1r5c8vX2yB7dF4hJ6lN8pR0tV2xZ4A"}
                    """))),
        @ApiResponse(responseCode = "400", description = "refreshToken 이 누락되었거나 공백입니다 (C0001) — 요청 본문을 확인해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"C0001","message":"리프레시 토큰을 입력해 주세요"}
                    """))),
        @ApiResponse(responseCode = "401",
            description = """
                리프레시 토큰 인증에 실패했습니다 — 다시 로그인해 주세요

                - `A0004` — 유효하지 않은 토큰 (미존재 또는 재사용 감지)
                - `A0005` — 만료된 토큰
                """,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "유효하지 않은 토큰 (A0004)", value = """
                        {"code":"A0004","message":"유효하지 않은 리프레시 토큰입니다. 다시 로그인해 주세요"}
                        """),
                    @ExampleObject(name = "만료된 토큰 (A0005)", value = """
                        {"code":"A0005","message":"리프레시 토큰이 만료되었습니다. 다시 로그인해 주세요"}
                        """)
                }))
    })
    TokenRefreshResponse refresh(TokenRefreshRequest request);
}
