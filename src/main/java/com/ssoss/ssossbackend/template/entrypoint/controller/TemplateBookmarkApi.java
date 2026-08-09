package com.ssoss.ssossbackend.template.entrypoint.controller;

import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.BookmarkedTemplateListResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "추천 템플릿")
interface TemplateBookmarkApi {

    @Operation(
        summary = "내 북마크 추천 템플릿 목록 조회",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            회원이 북마크해 둔 추천 템플릿을 모아서 조회합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 토큰의 회원이 북마크한 템플릿만 내려갑니다.
            - 페이징도 검색도 없습니다. 담아 둔 템플릿 전부가 한 번에 내려갑니다.
            - 최근에 북마크한 순서대로 정렬합니다.
            - 담아 둔 템플릿이 없으면 templates 가 빈 배열입니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "북마크한 템플릿 전부를 반환합니다",
            content = @Content(schema = @Schema(implementation = BookmarkedTemplateListResponse.class),
                examples = {
                    @ExampleObject(name = "2건", value = """
                        {
                          "templates": [
                            {
                              "id": 2,
                              "category": "EVENT",
                              "title": "이벤트 안내",
                              "description": "할인이나 이벤트 소식을 알리는 글",
                              "recommendedChannels": ["INSTAGRAM", "THREADS"]
                            },
                            {
                              "id": 1,
                              "category": "NEW_MENU",
                              "title": "신메뉴 출시 안내",
                              "description": "새로 나온 메뉴의 특징과 매력을 소개하는 글",
                              "recommendedChannels": ["INSTAGRAM", "BLOG", "THREADS"]
                            }
                          ]
                        }
                        """),
                    @ExampleObject(name = "담아 둔 템플릿 없음", value = """
                        {"templates":[]}
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
                    """)))
    })
    BookmarkedTemplateListResponse listBookmarked(Long memberId);

    @Operation(
        summary = "추천 템플릿 북마크 저장",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            추천 템플릿을 회원의 북마크 목록에 담습니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 토큰의 회원 기준으로 담깁니다.
            - 담은 템플릿은 목록·상세 조회에서 bookmarked 가 true 로 내려갑니다.
            - 이미 담아 둔 템플릿을 다시 호출해도 204 이고 북마크가 중복으로 쌓이지 않습니다. 재시도해도 결과가 같습니다.
            - 없는 템플릿을 담으려 하면 404(TP0001)입니다.
            - 다른 회원이 같은 템플릿을 담아도 서로 영향이 없습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "북마크했습니다"),
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
        @ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없습니다 (TP0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"TP0001","message":"템플릿을 찾을 수 없습니다"}
                    """)))
    })
    void bookmark(Long memberId, Long templateId);

    @Operation(
        summary = "추천 템플릿 북마크 해제",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            추천 템플릿을 회원의 북마크 목록에서 뺍니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 토큰의 회원 기준으로 빠집니다.
            - 뺀 템플릿은 목록·상세 조회에서 bookmarked 가 false 로 내려갑니다.
            - 담은 적 없거나 이미 뺀 템플릿, 없는 템플릿을 해제해도 204 입니다. 재시도해도 결과가 같습니다.
            - 다시 담으면 북마크 목록에 다시 들어갑니다.
            - 다른 회원이 같은 템플릿을 담아 둔 북마크는 그대로 남습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "북마크를 해제했습니다"),
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
    void unbookmark(Long memberId, Long templateId);
}
