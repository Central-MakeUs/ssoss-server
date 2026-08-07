package com.ssoss.ssossbackend.template.entrypoint.controller;

import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.template.entrypoint.request.TemplateListRequest;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateListResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "추천 템플릿")
interface TemplateApi {

    @Operation(
        summary = "추천 템플릿 목록 조회",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            운영자가 심어 둔 추천 템플릿을 카드 목록으로 조회합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 입니다. 목록 자체는 모든 회원에게 같습니다.
            - 카드 1건이 템플릿 하나이며, title 이 카드 제목이고 description 이 그 아래 한 줄입니다.
              recommendedChannels 는 이 템플릿을 올리기 좋은 채널이라 카드에 배지로 붙이면 됩니다.
            - 최근에 심은 템플릿이 먼저 오는 순서로 고정입니다. 정렬을 고르는 파라미터는 없습니다.
            - 분류 탭은 category 로 붙이세요. "전체" 탭은 분류값이 아니라 category 를 아예 보내지 않는 상태입니다.
              홈의 분류 바로가기도 이 API 를 category 와 함께 부르면 됩니다.
            - page 는 0 부터 세고 size 는 기본 20·최대 50 입니다. 아래로 넘길 때는 hasNext 가 true 인 동안 page 를 올려 부르세요.
              category 를 준 결과에도 페이징이 그대로 동작하고, totalCount 는 걸러진 개수입니다.
            - 카드에는 본문이 담기지 않습니다. 본문 미리보기는 별도 API 로 받습니다.
            - bookmarked 는 지금 항상 false 로 내려갑니다. 북마크 저장 기능이 붙으면 값만 채워지고 응답 모양은 그대로입니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "최근에 심은 템플릿부터 담은 카드 목록과 전체 건수를 반환합니다",
            content = @Content(schema = @Schema(implementation = TemplateListResponse.class),
                examples = @ExampleObject(name = "2건", value = """
                    {
                      "totalCount": 2,
                      "page": 0,
                      "size": 20,
                      "hasNext": false,
                      "templates": [
                        {
                          "id": 2,
                          "category": "EVENT",
                          "title": "오픈 기념 할인 안내",
                          "description": "문을 연 지 얼마 안 된 매장이 첫 할인을 알릴 때 쓰는 글입니다",
                          "recommendedChannels": ["DAANGN_BIZ", "INSTAGRAM", "BLOG"],
                          "bookmarked": false
                        },
                        {
                          "id": 1,
                          "category": "NEW_MENU",
                          "title": "신메뉴 출시 알림",
                          "description": "새로 나온 메뉴를 사진과 함께 처음 알릴 때 쓰는 글입니다",
                          "recommendedChannels": ["INSTAGRAM", "BLOG", "THREADS"],
                          "bookmarked": false
                        }
                      ]
                    }
                    """))),
        @ApiResponse(responseCode = "400", description = "category 가 분류값이 아니거나 page·size 가 허용 범위를 벗어났습니다 (C0001)",
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
    TemplateListResponse list(TemplateListRequest request);
}
