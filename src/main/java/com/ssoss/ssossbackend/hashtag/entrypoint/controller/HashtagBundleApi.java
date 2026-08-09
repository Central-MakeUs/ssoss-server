package com.ssoss.ssossbackend.hashtag.entrypoint.controller;

import com.ssoss.ssossbackend.hashtag.entrypoint.request.HashtagBundleListRequest;
import com.ssoss.ssossbackend.hashtag.entrypoint.response.HashtagBundleListResponse;
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
interface HashtagBundleApi {

    @Operation(
        summary = "해시태그 묶음 목록 조회",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            운영자가 심어 둔 해시태그 묶음을 카드 목록으로 조회합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 입니다. 묶음 목록 자체는 모두에게 같고 bookmarked 만 회원마다 다릅니다.
            - 카드 1건이 묶음 하나이며, name 이 카드 제목이고 hashtags 가 그 묶음의 태그 전부입니다.
              태그는 서버가 자르지 않으니 카드에 다 보여주거나 화면에서 접어 주세요. 개수는 hashtags 의 길이로 세면 됩니다.
            - 최근에 심은 묶음이 먼저 오는 순서로 고정입니다. 정렬을 고르는 파라미터는 없습니다.
            - 검색창은 keyword 로 붙이세요. 묶음 이름과 태그 내용을 함께 부분 일치로 거릅니다.
              "콘센트"만 쳐도 #콘센트많은카페 를 가진 묶음이 걸립니다. 앞뒤 공백은 무시합니다.
              비워 보내거나 생략하면 전체 목록이 내려옵니다. 검색 전용 API 는 따로 없습니다.
            - page 는 0 부터 세고 size 는 기본 20·최대 50 입니다. 아래로 넘길 때는 hasNext 가 true 인 동안 page 를 올려 부르세요.
              keyword 를 준 검색 결과에도 페이징이 그대로 동작하고, totalCount 는 걸러진 개수입니다.
            - 지역이 들어가는 태그는 회원이 직접 채우는 자리를 00 으로 심어 둡니다(예: #00동카페). 그대로 복사한 뒤 동네 이름으로 고쳐 쓰는 태그입니다.
            - 카드마다 bookmarked 가 함께 내려갑니다. 북마크 아이콘을 눌린 상태로 그릴 때 쓰세요.
              내 북마크 목록을 따로 받아 대조할 필요가 없습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "최근에 심은 묶음부터 담은 카드 목록과 전체 건수를 반환합니다",
            content = @Content(schema = @Schema(implementation = HashtagBundleListResponse.class),
                examples = @ExampleObject(name = "3건", value = """
                    {
                      "totalCount": 3,
                      "page": 0,
                      "size": 20,
                      "hasNext": false,
                      "bundles": [
                        {
                          "id": 3,
                          "name": "동네 고객 유입 해시태그",
                          "hashtags": ["#00동카페", "#00역카페", "#00동맛집", "#00동디저트", "#동네카페", "#우리동네카페"],
                          "bookmarked": false
                        },
                        {
                          "id": 2,
                          "name": "이벤트/할인 홍보",
                          "hashtags": ["#오픈이벤트", "#할인이벤트", "#신메뉴출시", "#1+1이벤트", "#선착순이벤트", "#오늘의쿠폰"],
                          "bookmarked": true
                        },
                        {
                          "id": 1,
                          "name": "카공 카페",
                          "hashtags": ["#카공카페", "#노트북카페", "#콘센트많은카페", "#조용한카페", "#공부하기좋은카페", "#스터디카페"],
                          "bookmarked": false
                        }
                      ]
                    }
                    """))),
        @ApiResponse(responseCode = "400", description = "page·size 가 허용 범위를 벗어났습니다 (C0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"C0001","message":"한 번에 최대 50건까지 조회할 수 있습니다"}
                    """))),
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
    HashtagBundleListResponse list(Long memberId, HashtagBundleListRequest request);
}
