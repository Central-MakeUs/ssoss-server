package com.ssoss.ssossbackend.auth.entrypoint.controller;

import com.ssoss.ssossbackend.auth.entrypoint.request.SignupRequest;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;
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
interface SignupApi {

    @Operation(
        summary = "회원가입",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            가입 대기(PENDING) 회원이 약관 동의를 제출해 회원가입합니다.

            **처리 순서**
            1. 소셜 로그인에서 받은 가입 대기(PENDING) accessToken 을 Bearer 헤더로 전달합니다.
               - 가입 대기 토큰 전용 API 입니다. 가입 회원(ACTIVE) 토큰으로 호출하면 403 을 응답합니다.
            2. 약관 3종(`serviceTermsAgreed`·`privacyPolicyAgreed`·`marketingAgreed`) 전체의 동의 여부를 제출합니다.
               - 서비스 이용약관·개인정보 수집·이용은 필수 — 동의하지 않으면 400 을 응답합니다.
               - 마케팅 수신은 선택 — 미동의(false)로도 회원가입이 되며, 미동의 사실과 시각이 기록됩니다.
            3. 완료되면 가입 회원(ACTIVE)으로 전환되고 role=ACTIVE 토큰 쌍(access + refresh)이 새로 발급됩니다.
               이후 보호 API 는 새 accessToken 으로 호출합니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원가입에 성공해 가입 회원 상태와 새 토큰 쌍을 반환합니다",
            content = @Content(schema = @Schema(implementation = SignupResponse.class),
                examples = @ExampleObject(value = """
                    {"status":"ACTIVE","accessToken":"eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxIn0.x","refreshToken":"3q2nq0uW9kZ0m1r5c8vX2yB7dF4hJ6lN8pR0tV2xZ4A"}
                    """))),
        @ApiResponse(responseCode = "400", description = """
            약관 필드가 누락되었거나 형식이 잘못되었습니다 (C0001) — 약관 3종 전체를 제출해야 합니다.
            또는 필수 약관에 동의하지 않았습니다 (T0001).
            """,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "약관 필드 누락(C0001)", value = """
                        {"code":"C0001","message":"마케팅 수신 동의 여부를 입력해 주세요"}
                        """),
                    @ExampleObject(name = "필수 약관 미동의(T0001)", value = """
                        {"code":"T0001","message":"필수 약관에 모두 동의해야 회원가입할 수 있습니다"}
                        """)
                })),
        @ApiResponse(responseCode = "401", description = "accessToken 이 없거나 유효하지 않습니다 (A0006) — 소셜 로그인 후 다시 호출해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"A0006","message":"유효하지 않은 인증 정보입니다. 다시 로그인해 주세요"}
                    """))),
        @ApiResponse(responseCode = "403", description = "가입 대기(PENDING) 토큰이 아닙니다 (A0007) — 이미 회원가입한 회원은 호출할 수 없습니다",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"A0007","message":"접근 권한이 없습니다"}
                    """))),
        @ApiResponse(responseCode = "409", description = "이미 회원가입한 회원입니다 (M0001) — 다시 로그인해 ACTIVE 토큰을 발급받아 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"M0001","message":"이미 회원가입한 회원입니다"}
                    """)))
    })
    SignupResponse signup(Long memberId, SignupRequest request);
}
