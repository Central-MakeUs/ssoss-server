package com.ssoss.ssossbackend.auth.entrypoint.controller;

import com.ssoss.ssossbackend.auth.entrypoint.request.LogoutRequest;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "인증")
interface LogoutApi {

    @Operation(
        summary = "로그아웃",
        description = """
            리프레시 토큰을 폐기해 현재 세션을 종료합니다.

            - 폐기 범위는 제출한 리프레시 토큰이 속한 세션뿐입니다. 다른 기기의 로그인 세션은 유지됩니다.
            - 멱등 API 입니다. 유효하지 않은(미존재·이미 폐기·만료) 토큰을 제출해도 항상 204 를 반환하므로, 클라이언트는 응답과 무관하게 보관 중인 토큰을 삭제하면 됩니다.
            - 액세스 토큰은 즉시 무효화되지 않고 자체 만료 시각까지 유효합니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "로그아웃 처리되었습니다 (토큰 유효 여부와 무관)"),
        @ApiResponse(responseCode = "400", description = "refreshToken 이 누락되었거나 공백입니다 (C0001) — 요청 본문을 확인해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"C0001","message":"리프레시 토큰을 입력해 주세요"}
                    """)))
    })
    void logout(LogoutRequest request);
}
