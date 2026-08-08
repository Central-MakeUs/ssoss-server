package com.ssoss.ssossbackend.template.entrypoint.controller;

import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.template.entrypoint.request.TemplateListRequest;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateAppliedResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.TemplateDetailResponse;
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

            - 가입 회원(ACTIVE) accessToken 전용 API 입니다. 템플릿 목록 자체는 모두에게 같고 bookmarked 만 회원마다 다릅니다.
            - 카드 1건이 템플릿 하나이며, title 이 카드 제목이고 description 이 그 아래 한 줄입니다.
              recommendedChannels 는 이 템플릿을 올리기 좋은 채널이라 카드에 배지로 붙이면 됩니다.
            - 최근에 심은 템플릿이 먼저 오는 순서로 고정입니다. 정렬을 고르는 파라미터는 없습니다.
            - 분류 탭은 category 로 붙이세요. "전체" 탭은 분류값이 아니라 category 를 아예 보내지 않는 상태입니다.
              홈의 분류 바로가기도 이 API 를 category 와 함께 부르면 됩니다.
            - page 는 0 부터 세고 size 는 기본 20·최대 50 입니다. 아래로 넘길 때는 hasNext 가 true 인 동안 page 를 올려 부르세요.
              category 를 준 결과에도 페이징이 그대로 동작하고, totalCount 는 걸러진 개수입니다.
            - 카드에는 본문이 담기지 않습니다. 본문 미리보기는 별도 API 로 받습니다.
            - bookmarked 는 토큰의 회원이 그 템플릿을 북마크했는지입니다. 북마크 저장은 별도 API 로 합니다.
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
                          "description": "문을 연 지 얼마 안 된 가게가 첫 할인을 알리는 글",
                          "recommendedChannels": ["DAANGN_BIZ", "INSTAGRAM", "BLOG"],
                          "bookmarked": false
                        },
                        {
                          "id": 1,
                          "category": "NEW_MENU",
                          "title": "신메뉴 출시 안내",
                          "description": "새로 나온 메뉴의 특징과 매력을 소개하는 글",
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
    TemplateListResponse list(Long memberId, TemplateListRequest request);

    @Operation(
        summary = "추천 템플릿 상세 조회",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            목록에서 고른 템플릿 하나의 상세를 조회합니다. 적용하기를 누르기 전에 본문을 보는 자리입니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 입니다. 상세 자체는 모두에게 같고 bookmarked 만 회원마다 다릅니다.
            - body 와 exampleBody 는 서로 다른 글입니다. body 는 `[가게명]`·`[주소]` 같은 대괄호 자리표시자가 그대로 남은 틀이고,
              exampleBody 는 다른 매장 정보로 전부 채워진 완성 글이라 "완성되면 이런 모양"을 보여 주는 데 씁니다.
            - 서버는 치환을 하지 않습니다. 내 매장 정보로 채운 본문은 적용 API 로 받습니다.
            - category·title·description·recommendedChannels 는 목록 카드와 같은 값이라 화면 상단을 그대로 그릴 수 있습니다.
            - bookmarked 는 토큰의 회원이 이 템플릿을 북마크했는지이며, 목록 카드의 값과 같습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "자리표시자가 남은 원문과 채워진 예시 본문을 함께 반환합니다",
            content = @Content(schema = @Schema(implementation = TemplateDetailResponse.class),
                examples = @ExampleObject(name = "신메뉴 출시 안내", value = """
                    {
                      "id": 8,
                      "category": "NEW_MENU",
                      "title": "신메뉴 출시 안내",
                      "description": "새로 나온 메뉴의 특징과 매력을 소개하는 글",
                      "body": "[가게명]에 새 메뉴가 출시되었습니다!\\n\\n🎁신메뉴: [메뉴명]\\n💰가격: [가격]원\\n\\n📍[주소]",
                      "exampleBody": "카페 모먼트에 새 메뉴가 출시되었습니다!\\n\\n🎁신메뉴: 피스타치오 크림 라떼\\n💰가격: 6,500원\\n\\n📍서울 성동구 서울숲2길 14",
                      "recommendedChannels": ["INSTAGRAM", "BLOG", "THREADS"],
                      "bookmarked": false
                    }
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
                    """))),
        @ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없습니다 (TP0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"TP0001","message":"템플릿을 찾을 수 없습니다"}
                    """)))
    })
    TemplateDetailResponse getById(Long memberId, Long templateId);

    @Operation(
        summary = "추천 템플릿 적용",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            상세에서 적용하기를 누른 템플릿의 본문을 내 매장 정보로 채워 조회합니다. 편집 화면을 여는 자리입니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이고, 회원마다 자기 매장 정보로 채워진 서로 다른 본문을 받습니다.
            - 저장이 아닙니다. 이 API 는 아무것도 남기지 않으므로 편집한 본문은 저장 API 로 따로 보내야 합니다.
            - 본문의 대괄호 자리표시자 가운데 내 매장 정보로 채울 수 있는 것만 그 값으로 바뀝니다.
              채울 값이 없으면 자리표시자가 그대로 남고, 남은 자리는 회원이 편집 화면에서 채웁니다.
            - 판정은 자리표시자마다 따로입니다. 매장 정보를 일부만 입력한 회원은 입력한 항목만 채워집니다.
            - 상세 조회(`GET /v1/templates/{templateId}`)의 body 는 치환하지 않은 원문이라 이 API 의 body 와 다릅니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "매장 정보로 자리표시자를 채운 본문을 반환합니다",
            content = @Content(schema = @Schema(implementation = TemplateAppliedResponse.class),
                examples = @ExampleObject(name = "채울 수 있는 자리표시자만 바뀐 경우", value = """
                    {
                      "id": 8,
                      "body": "보니스커피에 새 메뉴가 출시되었습니다!\\n\\n🎁신메뉴: [메뉴명]\\n💰가격: [가격]원\\n\\n📍서울 중구 을지로 100\\n🕐영업시간: 수, 목, 금, 토, 일 오전 9:00 ~ 오후 8:00\\n📞[전화번호]"
                    }
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
                    """))),
        @ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없습니다 (TP0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"TP0001","message":"템플릿을 찾을 수 없습니다"}
                    """)))
    })
    TemplateAppliedResponse getApplied(Long memberId, Long templateId);
}
