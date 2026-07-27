package com.ssoss.ssossbackend.app.entrypoint.controller;

import com.ssoss.ssossbackend.app.entrypoint.request.AppVersionCheckRequest;
import com.ssoss.ssossbackend.app.entrypoint.response.AppVersionResponse;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "앱")
interface AppVersionApi {

    @Operation(
        summary = "앱 버전 조회",
        description = """
            보낸 앱 버전이 계속 이용할 수 있는 버전인지 확인합니다.

            - 인증이 필요 없는 API 입니다. 앱 실행 직후 로그인 전에 호출합니다.
            - 최소 지원 버전은 OS 마다 따로 정합니다. iOS 는 CFBundleShortVersionString, Android 는 versionName 을 보냅니다.
            - 보낸 버전이 최소 지원 버전보다 낮으면 updateRequired 가 true 입니다. 같은 버전은 이용할 수 있어 false 입니다.
            - 조회가 실패하면 사용자를 막지 말고 그대로 이용하게 해 주세요.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "업데이트 필요 여부와 최소 지원 버전을 반환합니다"),
        @ApiResponse(responseCode = "400", description = """
            지원하지 않는 OS 입니다 (AP0001) / 앱 버전 형식이 올바르지 않습니다 (AP0003) / 앱 버전을 보내지 않았습니다 (C0001)
            """,
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"AP0003","message":"앱 버전 형식이 올바르지 않습니다"}
                    """)))
    })
    AppVersionResponse check(
        @Parameter(description = "OS (대소문자 무관)", example = "IOS",
            schema = @Schema(allowableValues = {"IOS", "ANDROID"})) String os,
        AppVersionCheckRequest request
    );
}
