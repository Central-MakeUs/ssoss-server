package com.ssoss.ssossbackend.auth.entrypoint.controller;

import com.ssoss.ssossbackend.auth.entrypoint.request.SocialLoginRequest;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "인증")
interface SocialLoginApi {

    @Operation(
        summary = "소셜 로그인",
        description = """
            소셜 로그인 프로바이더 SDK로 발급받은 액세스 토큰을 전달하면 서버 자체 토큰을 발급합니다.

            **처리 순서**
            1. 앱이 프로바이더 SDK 로그인으로 액세스 토큰을 발급받습니다.
            2. 이 API 에 액세스 토큰을 전달합니다.
            3. 서버가 프로바이더에 토큰 유효성을 확인합니다. 무효한 토큰이면 401 을 응답합니다.
            4. 검증에 성공하면 서버 자체 토큰(access JWT + opaque refresh)을 응답합니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인에 성공해 서버 자체 발급 토큰 쌍을 반환합니다",
            content = @Content(schema = @Schema(implementation = SocialLoginResponse.class),
                examples = @ExampleObject(value = """
                    {"accessToken":"eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxIn0.x","refreshToken":"3q2nq0uW9kZ0m1r5c8vX2yB7dF4hJ6lN8pR0tV2xZ4A"}
                    """))),
        @ApiResponse(responseCode = "400", description = "accessToken 이 누락되었거나 공백입니다 (C0001) — 요청 본문을 확인해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"C0001","message":"소셜 액세스 토큰을 입력해 주세요"}
                    """))),
        @ApiResponse(responseCode = "401", description = "프로바이더가 액세스 토큰을 거부했습니다 (A0001) — 프로바이더 SDK 재로그인 후 다시 호출해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"A0001","message":"소셜 인증에 실패했습니다. 다시 로그인해 주세요"}
                    """))),
        @ApiResponse(responseCode = "404", description = "지원하지 않는 프로바이더입니다 (A0002) — {provider} 경로 값을 확인해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"A0002","message":"지원하지 않는 소셜 로그인입니다"}
                    """))),
        @ApiResponse(responseCode = "503", description = "소셜 프로바이더 장애입니다 (A0003) — 잠시 후 다시 시도해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"A0003","message":"소셜 로그인 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해 주세요"}
                    """)))
    })
    SocialLoginResponse login(
        @Parameter(description = "소셜 프로바이더 (대소문자 무관)", example = "naver",
            schema = @Schema(allowableValues = {"naver"})) String provider,
        SocialLoginRequest request
    );
}
