package com.ssoss.ssossbackend.content.entrypoint.controller;

import com.ssoss.ssossbackend.content.entrypoint.request.StyleReuseRequest;
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

@Tag(name = "스타일 재사용")
interface StyleReuseApi {

    @Operation(
        summary = "이 스타일로 새로 만들기",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            저장한 채널별 콘텐츠 하나의 말투·구성·분량을 참고해 같은 채널의 콘텐츠를 새로 만듭니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 본인의 콘텐츠만 원본으로 쓸 수 있습니다.
            - 원본은 저장한 채널별 콘텐츠 1건이고 만들어지는 것도 같은 채널 1건입니다.
              상세 화면에서 보고 있는 탭의 contentChannelId 를 그대로 경로에 넣으면 그 채널이 나옵니다.
            - 채널과 목적·톤은 요청에 담지 않습니다. 채널은 원본과 같고, 목적·톤은 원본 콘텐츠에 저장된 값을 서버가 읽어 씁니다.
              요청에 담는 것은 강조 내용(필수)·금지 내용·키워드·사진 가이드 체크 넷뿐입니다.
            - 원본에서 가져오는 것은 말투·구성·분량뿐이고 소재는 새 강조 내용에서만 나옵니다.
              원본에 있던 메뉴·소식이 새 글에 섞이지 않도록 서버가 막으므로 금지 내용에 따로 적지 않아도 됩니다.
            - 원본으로 쓰는 글은 요청 시점의 최신본입니다. 편집한 뒤에 부르면 편집한 글이 참고됩니다.
            - 만들어진 결과는 신규 생성과 똑같이 동작합니다. 반환된 작업 id 로 생성 작업 조회를 반복 호출해 결과를 받고, 저장하기로 저장합니다.
              저장하면 채널이 하나뿐인 새 콘텐츠가 생기고 원본 콘텐츠는 그대로 남습니다.
            - 회원당 진행 중 작업은 1건으로 제한됩니다. 진행 중 작업이 있으면 409 로 거부됩니다.
            - 1채널이라 크레딧은 성공한 작업 1건이 5 를 차감합니다. 잔액이 5 보다 적으면 400 으로 거부됩니다.
            - 작업은 생성 시각부터 60초 안에 끝나지 못하면 실패합니다.
            - 같은 원본으로 몇 번이든 다시 부를 수 있고, 각각 독립된 작업이 됩니다. 재사용한 뒤 원본을 삭제해도 만들어진 작업과 결과는 남습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "생성 작업이 만들어졌습니다. Location 헤더와 본문의 작업 id 로 생성 작업 조회를 호출하세요",
            content = @Content(schema = @Schema(implementation = GenerationStartResponse.class),
                examples = @ExampleObject(value = """
                    {"generationId": 12}
                    """))),
        @ApiResponse(responseCode = "400",
            description = "입력값이 잘못되었거나 (C0001 — 강조 내용 누락 등) 크레딧이 부족합니다 (CR0002)",
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
    ResponseEntity<GenerationStartResponse> reuse(Long memberId, Long contentId, Long contentChannelId,
        StyleReuseRequest request);
}
