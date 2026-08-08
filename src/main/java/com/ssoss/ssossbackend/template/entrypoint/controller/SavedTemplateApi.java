package com.ssoss.ssossbackend.template.entrypoint.controller;

import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.template.entrypoint.request.SavedTemplateEditRequest;
import com.ssoss.ssossbackend.template.entrypoint.request.SavedTemplateListRequest;
import com.ssoss.ssossbackend.template.entrypoint.request.SavedTemplateSaveRequest;
import com.ssoss.ssossbackend.template.entrypoint.response.SavedTemplateDetailResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.SavedTemplateListResponse;
import com.ssoss.ssossbackend.template.entrypoint.response.SavedTemplateSaveResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;

@Tag(name = "추천 템플릿")
interface SavedTemplateApi {

    @Operation(
        summary = "템플릿 저장하기",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            편집 화면에서 다듬은 본문을 회원의 글로 저장합니다. 저장하기를 눌렀을 때 부르는 API 입니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 저장한 글은 부른 회원의 것이 됩니다.
            - 적용(`GET /v1/templates/{templateId}/applied`)은 아무것도 남기지 않으므로, 저장은 이 API 로만 이뤄집니다.
            - body 는 화면에 보이는 최종 본문을 그대로 보냅니다. 서버가 자리표시자를 다시 채우거나 원본 본문으로 덮어쓰지 않습니다.
            - 제목·설명·분류·추천 채널은 저장 시점에 원본 템플릿에서 복사합니다.
              운영자가 나중에 원본을 고쳐도 저장한 글은 그대로입니다. 예시 본문은 복사하지 않습니다.
            - 같은 템플릿을 여러 번 저장하면 저장한 만큼 각각 남습니다. 같은 템플릿으로 이번 달과 다음 달에 서로 다른 글을 쓰기 위해서입니다.
            - body 는 2000자까지이고, 비어 있거나 공백만 있으면 400(C0001)입니다.
            - AI 를 거치지 않으므로 크레딧이 차감되지 않습니다.
            - 응답의 savedTemplateId 는 저장한 글의 id 라 요청에 보낸 templateId 와 다른 값입니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "저장한 글의 id 를 반환합니다",
            content = @Content(schema = @Schema(implementation = SavedTemplateSaveResponse.class),
                examples = @ExampleObject(value = """
                    {"savedTemplateId": 1}
                    """))),
        @ApiResponse(responseCode = "400", description = "templateId 가 없거나 body 가 비었거나 2000자를 넘었습니다 (C0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"C0001","message":"본문은 2000자 이내로 입력해 주세요"}
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
    ResponseEntity<SavedTemplateSaveResponse> save(Long memberId, SavedTemplateSaveRequest request);

    @Operation(
        summary = "저장한 템플릿 목록 조회",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            회원이 저장한 글을 저장 시각순으로 조회합니다. 저장 내역 화면에서 부르는 API 입니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 본인이 저장한 글만 조회됩니다.
            - 카드 1건이 저장하기 1회입니다. 같은 템플릿을 여러 번 저장했다면 저장한 만큼 각각 카드로 나옵니다.
            - 정렬 기준은 저장 시각이고 sort 로 방향만 고릅니다. LATEST 는 최신순, OLDEST 는 오래된 순이며 생략하면 LATEST 입니다.
            - page 는 0 부터 세고 size 는 기본 20·최대 50 입니다. 아래로 넘길 때는 hasNext 가 true 인 동안 page 를 올려 부르세요.
            - category·title·description 은 저장 시점에 원본 템플릿에서 복사한 값이라, 운영자가 원본을 고쳐도 움직이지 않습니다.
            - 채널로 거르는 파라미터는 없습니다. 저장한 글에는 채널이 없어 생성 기록 목록과 섞이지 않습니다.
            - 분류로 거르는 파라미터도 없습니다. 분류는 카드 배지로만 쓰입니다.
            - 본문은 담기지 않습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "고른 정렬 방향의 카드 목록과 전체 건수를 반환합니다",
            content = @Content(schema = @Schema(implementation = SavedTemplateListResponse.class),
                examples = @ExampleObject(name = "2건", value = """
                    {
                      "totalCount": 2,
                      "page": 0,
                      "size": 20,
                      "hasNext": false,
                      "savedTemplates": [
                        {
                          "savedTemplateId": 2,
                          "category": "EVENT",
                          "title": "오픈 기념 할인 안내",
                          "description": "문을 연 지 얼마 안 된 가게가 첫 할인을 알리는 글",
                          "savedAt": "2026-09-01T09:41:00Z"
                        },
                        {
                          "savedTemplateId": 1,
                          "category": "NEW_MENU",
                          "title": "신메뉴 출시 안내",
                          "description": "새로 나온 메뉴의 특징과 매력을 소개하는 글",
                          "savedAt": "2026-08-23T02:10:00Z"
                        }
                      ]
                    }
                    """))),
        @ApiResponse(responseCode = "400", description = "sort 가 없는 값이거나 page·size 가 허용 범위를 벗어났습니다 (C0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "size 상한 초과", value = """
                        {"code":"C0001","message":"한 번에 최대 50건까지 조회할 수 있습니다"}
                        """),
                    @ExampleObject(name = "없는 정렬", value = """
                        {"code":"C0001","message":"입력값을 다시 확인해 주세요"}
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
    SavedTemplateListResponse list(Long memberId, SavedTemplateListRequest request);

    @Operation(
        summary = "저장한 템플릿 상세 조회",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            저장 내역에서 글 하나를 열어 봅니다. 내가 저장한 글만 볼 수 있습니다.

            - 모든 값이 저장 시점에 복사해 둔 것이라, 운영자가 원본 템플릿을 고치거나 지워도 움직이지 않습니다.
            - body 는 저장할 때 보낸 본문 그대로입니다. 서버가 자리표시자를 채우지 않으므로, 저장할 때 남겨 둔 자리표시자는 그대로 담겨 나옵니다.
            - 남의 글과 없는 id 는 똑같이 404 입니다. 존재 여부를 알려주지 않습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "저장한 글의 본문과 카드 정보를 반환합니다",
            content = @Content(schema = @Schema(implementation = SavedTemplateDetailResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "savedTemplateId": 1,
                      "category": "NEW_MENU",
                      "title": "신메뉴 출시 안내",
                      "description": "새로 나온 메뉴의 특징과 매력을 소개하는 글",
                      "body": "보니스커피에 새 메뉴가 출시되었습니다!",
                      "recommendedChannels": ["INSTAGRAM", "BLOG", "THREADS"],
                      "savedAt": "2026-09-01T09:41:00Z"
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
        @ApiResponse(responseCode = "404", description = "저장한 템플릿을 찾을 수 없습니다 (TP0002) — 없는 id 이거나 남의 글입니다",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"TP0002","message":"저장한 템플릿을 찾을 수 없습니다"}
                    """)))
    })
    SavedTemplateDetailResponse getById(Long memberId, Long savedTemplateId);

    @Operation(
        summary = "저장한 템플릿 편집",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            저장한 글의 제목과 본문을 고칩니다. 내가 저장한 글만 고칠 수 있습니다.

            - title 과 body 를 모두 보내는 덮어쓰기입니다. 한쪽만 고칠 때는 나머지는 지금 값을 그대로 실어 보내세요.
            - 제목을 고칠 수 있는 것은 같은 템플릿에서 저장한 글끼리 목록에서 구분하기 위해서입니다.
            - 분류·설명·추천 채널은 저장 시점에 복사한 값 그대로 두고 편집 대상이 아닙니다.
            - 응답은 상세 조회와 같은 모양이고 savedAt 은 저장 시각이라 편집해도 움직이지 않습니다.
            - title 은 100자, body 는 2000자까지이며 둘 다 비어 있거나 공백만 있으면 400(C0001)입니다.
            - 값이 지금과 같으면 아무것도 바꾸지 않고 200 으로 지금 값을 그대로 돌려줍니다.
            - 원본 템플릿은 이 API 로 바뀌지 않습니다. 저장한 글은 복사본이라 원본과 따로 움직입니다.
            - 남의 글과 없는 id 는 똑같이 404 입니다. 존재 여부를 알려주지 않습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "고친 뒤의 저장한 글을 반환합니다",
            content = @Content(schema = @Schema(implementation = SavedTemplateDetailResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "savedTemplateId": 1,
                      "category": "NEW_MENU",
                      "title": "9월 신메뉴 안내",
                      "description": "새로 나온 메뉴의 특징과 매력을 소개하는 글",
                      "body": "보니스커피에 흑임자 라떼가 새로 나왔습니다!",
                      "recommendedChannels": ["INSTAGRAM", "BLOG", "THREADS"],
                      "savedAt": "2026-09-01T09:41:00Z"
                    }
                    """))),
        @ApiResponse(responseCode = "400", description = "제목이나 본문이 비었거나 길이 상한을 넘었습니다 (C0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "제목 상한 초과", value = """
                        {"code":"C0001","message":"제목은 100자 이내로 입력해 주세요"}
                        """),
                    @ExampleObject(name = "본문 상한 초과", value = """
                        {"code":"C0001","message":"본문은 2000자 이내로 입력해 주세요"}
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
        @ApiResponse(responseCode = "404", description = "저장한 템플릿을 찾을 수 없습니다 (TP0002) — 없는 id 이거나 남의 글입니다",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"TP0002","message":"저장한 템플릿을 찾을 수 없습니다"}
                    """)))
    })
    SavedTemplateDetailResponse edit(Long memberId, Long savedTemplateId, SavedTemplateEditRequest request);

    @Operation(
        summary = "저장한 템플릿 삭제",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            저장한 글 1건을 삭제합니다. 내가 저장한 글만 삭제할 수 있습니다.

            - 삭제한 글은 목록·전체 건수에서 빠지고, 상세·편집·삭제를 다시 호출하면 404(TP0002)입니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제되었습니다"),
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
        @ApiResponse(responseCode = "404", description = "저장한 템플릿을 찾을 수 없습니다 (TP0002) — 없는 id 이거나 남의 글이거나 이미 삭제했습니다",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"TP0002","message":"저장한 템플릿을 찾을 수 없습니다"}
                    """)))
    })
    void delete(Long memberId, Long savedTemplateId);
}
