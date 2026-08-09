package com.ssoss.ssossbackend.content.entrypoint.controller;

import com.ssoss.ssossbackend.content.entrypoint.request.ChannelConversionRequest;
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

@Tag(name = "다른 채널용으로 만들기")
interface ChannelConversionApi {

    @Operation(
        summary = "다른 채널용으로 만들기",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            저장한 채널별 콘텐츠 하나를 원본으로 삼아 다른 채널의 콘텐츠를 새로 만듭니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 본인의 콘텐츠만 원본으로 쓸 수 있습니다.
            - 원본은 저장한 채널별 콘텐츠 1건입니다. 상세 화면에서 보고 있는 탭의 contentChannelId 를 그대로 경로에 넣으세요.
            - 요청에 담는 것은 새로 만들 채널 목록뿐입니다. 원본과 같은 채널은 고를 수 없어 한 번에 1~3개까지 고를 수 있습니다.
            - 목적·톤·강조 내용·금지 내용·키워드·사진 가이드 체크는 요청에 담지 않습니다.
              원본을 만들어 낸 생성 작업에 넣었던 값을 서버가 그대로 읽어 새 작업에 다시 씁니다.
              그중 강조 내용·금지 내용·사진 가이드 체크는 어떤 조회 API 에도 나오지 않아 앱이 알 수 없는 값이라, 이 API 로만 이어받을 수 있습니다.
            - 새 글은 그 입력으로 새 채널에서 처음부터 다시 만들어집니다. 원본이 만들어 낸 제목·본문은 재료로 쓰지 않습니다.
              원본을 편집해 두어도 새 글에는 영향이 없습니다 — 같은 입력에서 그 채널에 맞는 글이 새로 나옵니다.
            - 만들어진 결과는 신규 생성과 똑같이 동작합니다. 반환된 작업 id 로 생성 작업 조회를 반복 호출해 결과를 받고, 저장하기로 저장합니다.
              저장하면 고른 채널만 담긴 새 콘텐츠가 생기고 원본 콘텐츠는 그대로 남습니다.
            - 회원당 진행 중 작업은 1건으로 제한됩니다. 진행 중 작업이 있으면 409 로 거부됩니다.
            - 크레딧은 성공한 작업 1건이 고른 채널 수 × 5 를 차감합니다. 잔액이 모자라면 400 으로 거부됩니다.
            - 작업은 생성 시각부터 60초 안에 끝나지 못하면 실패합니다.
            - 같은 원본으로 몇 번이든 다시 부를 수 있고, 각각 독립된 작업이 됩니다. 만든 뒤 원본을 삭제해도 만들어진 작업과 결과는 남습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "생성 작업이 만들어졌습니다. Location 헤더와 본문의 작업 id 로 생성 작업 조회를 호출하세요",
            content = @Content(schema = @Schema(implementation = GenerationStartResponse.class),
                examples = @ExampleObject(value = """
                    {"generationId": 12}
                    """))),
        @ApiResponse(responseCode = "400",
            description = "입력값이 잘못되었거나 (C0001 — 채널 누락·중복·3개 초과 등) 원본과 같은 채널을 골랐거나 (CT0011) 크레딧이 부족합니다 (CR0002)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "입력값 오류", value = """
                        {"code":"C0001","message":"올바르지 않은 입력입니다"}
                        """),
                    @ExampleObject(name = "원본과 같은 채널", value = """
                        {"code":"CT0011","message":"원본과 같은 채널은 고를 수 없습니다"}
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
        @ApiResponse(responseCode = "404",
            description = "콘텐츠가 없거나 본인의 콘텐츠가 아니거나 삭제한 콘텐츠거나 (CT0005) 그 콘텐츠에 없는 채널입니다 (CT0006)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "콘텐츠 없음", value = """
                        {"code":"CT0005","message":"콘텐츠를 찾을 수 없습니다"}
                        """),
                    @ExampleObject(name = "채널별 콘텐츠 없음", value = """
                        {"code":"CT0006","message":"채널별 콘텐츠를 찾을 수 없습니다"}
                        """)})),
        @ApiResponse(responseCode = "409", description = "진행 중인 생성 작업이 이미 있습니다 (CT0001) — 완료된 뒤 다시 시도해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"CT0001","message":"진행 중인 생성 작업이 있습니다. 완료된 뒤 다시 시도해 주세요"}
                    """)))
    })
    ResponseEntity<GenerationStartResponse> convert(Long memberId, Long contentId, Long contentChannelId,
        ChannelConversionRequest request);
}
