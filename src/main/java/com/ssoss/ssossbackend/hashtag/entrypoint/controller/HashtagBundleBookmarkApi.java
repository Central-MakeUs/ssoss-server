package com.ssoss.ssossbackend.hashtag.entrypoint.controller;

import com.ssoss.ssossbackend.hashtag.entrypoint.response.BookmarkedHashtagBundleListResponse;
import com.ssoss.ssossbackend.shared.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "해시태그")
interface HashtagBundleBookmarkApi {

    @Operation(
        summary = "내 북마크 해시태그 묶음 목록 조회",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            회원이 북마크해 둔 해시태그 묶음을 모아서 조회합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 토큰의 회원이 북마크한 묶음만 내려갑니다.
            - 페이징도 검색도 없습니다. 담아 둔 묶음 전부가 한 번에 내려갑니다.
            - 최근에 북마크한 순서대로 정렬합니다.
            - 담아 둔 묶음이 없으면 bundles 가 빈 배열입니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "북마크한 묶음 전부를 반환합니다",
            content = @Content(schema = @Schema(implementation = BookmarkedHashtagBundleListResponse.class),
                examples = {
                    @ExampleObject(name = "2건", value = """
                        {
                          "bundles": [
                            {
                              "id": 2,
                              "name": "이벤트/할인 홍보",
                              "hashtags": ["#오픈이벤트", "#할인이벤트", "#신메뉴출시", "#1+1이벤트", "#선착순이벤트", "#오늘의쿠폰"]
                            },
                            {
                              "id": 1,
                              "name": "카공 카페",
                              "hashtags": ["#카공카페", "#노트북카페", "#콘센트많은카페", "#조용한카페", "#공부하기좋은카페", "#스터디카페"]
                            }
                          ]
                        }
                        """),
                    @ExampleObject(name = "담아 둔 묶음 없음", value = """
                        {"bundles":[]}
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
    BookmarkedHashtagBundleListResponse listBookmarked(Long memberId);

    @Operation(
        summary = "해시태그 묶음 북마크 저장",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            해시태그 묶음을 회원의 북마크 목록에 담습니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 토큰의 회원 목록에 담깁니다.
            - 담은 묶음은 내 북마크 해시태그 묶음 목록 조회에 나타납니다.
            - 이미 담아 둔 묶음을 다시 호출해도 204 이고 목록에는 하나만 남습니다. 재시도해도 결과가 같습니다.
            - 없는 묶음을 담으려 하면 404(HT0001)입니다.
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
        @ApiResponse(responseCode = "404", description = "해시태그 묶음을 찾을 수 없습니다 (HT0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"HT0001","message":"해시태그 묶음을 찾을 수 없습니다"}
                    """)))
    })
    void bookmark(Long memberId, Long bundleId);

    @Operation(
        summary = "해시태그 묶음 북마크 해제",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            해시태그 묶음을 회원의 북마크 목록에서 뺍니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 토큰의 회원이 담아 둔 목록에서 뺍니다.
            - 뺀 묶음은 내 북마크 해시태그 묶음 목록 조회에 더 이상 나타나지 않습니다.
            - 이미 뺀 묶음을 다시 호출해도 204 이고, 북마크한 적 없는 묶음이나 없는 묶음을 호출해도 204 입니다.
              어느 경우든 호출한 뒤의 결과가 "그 묶음은 내 목록에 없다"로 같아, 재시도해도 안전합니다.
            - 다른 회원이 같은 묶음을 북마크한 것은 그대로 남습니다.
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
    void unbookmark(Long memberId, Long bundleId);
}
