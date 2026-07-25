package com.ssoss.ssossbackend.content.entrypoint.controller;

import com.ssoss.ssossbackend.content.entrypoint.request.GenerationStartRequest;
import com.ssoss.ssossbackend.content.entrypoint.response.GenerationDetailResponse;
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

@Tag(name = "생성 작업")
interface GenerationApi {

    @Operation(
        summary = "생성 작업 생성",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            선택한 채널별 AI 콘텐츠를 만드는 생성 작업을 만들고 작업 id 를 즉시 반환합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 입니다.
            - 콘텐츠는 비동기로 생성됩니다. 반환된 작업 id 로 조회 API 를 호출해 채널별 결과를 확인하세요.
            - 회원당 진행 중 작업은 1건으로 제한됩니다. 진행 중 작업이 있으면 409 로 거부됩니다.
            - 크레딧 잔액이 차감량(5) × 선택 채널 수보다 적으면 400 으로 거부됩니다. 성공한 채널 결과 1건마다 5 가 차감됩니다.
            - 작업은 생성 시각부터 60초가 지나면 더 이상 결과가 더해지지 않습니다.
            - photoGuideChecked 를 true 로 보내면 어떤 사진을 찍어 넣으면 좋을지 알려 주는 안내가 결과 본문에 태그로 함께 담깁니다.
              보내지 않으면 false 로 보고 태그 없는 본문을 만듭니다. 태그 형식과 응답 모양은 조회 API 의 예시를 참고하세요.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 작업이 만들어졌습니다. Location 헤더와 본문의 작업 id 로 조회할 수 있습니다"),
        @ApiResponse(responseCode = "400",
            description = "입력값이 잘못되었거나 (C0001 — 채널 0개·중복 채널·목적/톤/강조 내용 누락 등) 크레딧이 부족합니다 (CR0002)",
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
        @ApiResponse(responseCode = "409", description = "진행 중인 생성 작업이 이미 있습니다 (CT0001) — 완료된 뒤 다시 시도해 주세요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"CT0001","message":"진행 중인 생성 작업이 있습니다. 완료된 뒤 다시 시도해 주세요"}
                    """)))
    })
    ResponseEntity<GenerationStartResponse> start(Long memberId, GenerationStartRequest request);

    @Operation(
        summary = "생성 작업 조회",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            생성 작업 하나의 진행 상태와 선택한 채널 전부의 결과를 조회합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 본인의 작업만 조회할 수 있습니다.
            - results 에는 선택한 채널이 요청한 순서 그대로 전부 담깁니다. 결과 화면의 채널 탭을 이 배열로 그릴 수 있습니다.
            - 채널별 status 는 PENDING(생성 중)·SUCCEEDED(성공)·FAILED(실패) 셋이고, message 는 상태와 무관하게 항상 값이 있습니다.
              성공한 채널만 본문이 차고, 제목은 성공한 블로그에만 있습니다.
            - 생성이 끝나기를 기다릴 때는 status 가 COMPLETED 가 될 때까지 이 API 를 반복 호출하세요.
              호출 간격과 중단 조건은 클라이언트 소관입니다.
            - COMPLETED 는 작업이 끝나 더 이상 결과가 더해지지 않는다는 뜻이지 전부 성공했다는 뜻이 아닙니다.
              채널마다 독립적으로 성공하거나 실패하므로 성공·실패 여부는 results 의 채널별 status 로 판단하세요.
            - COMPLETED 가 되면 새 생성 요청이 409 로 막히지 않습니다. 결과가 다 나왔는데도 IN_PROGRESS 인 짧은 구간이 있을 수 있는데,
              이때는 아직 새 생성을 시작할 수 없으므로 다시 생성하기 버튼은 COMPLETED 를 기준으로 열어 주세요.
            - 실패한 채널은 재시도(새 생성 요청)로 복구합니다.
            - purpose·tone·keywords 는 생성에 쓴 조건입니다. 작업 id 로 다시 들어와도 결과 화면 상단을 그릴 수 있도록 함께 내려줍니다.
            - 사진 가이드를 체크하고 만든 작업이면 본문 안에 `<photo-guide />` 태그가 섞여 옵니다.
              닫는 태그가 없는 한 덩어리이고 type 은 MENU·STORE·MOOD·PEOPLE 넷 중 하나이며, 클라이언트가 이 태그를 찾아 사진 안내 카드로 그립니다.
              태그를 그대로 두면 사용자에게 노출되므로 반드시 파싱해 주세요. 체크하지 않고 만든 작업의 본문에는 태그가 없습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "작업 상태와 선택 채널 전부의 결과를 반환합니다",
            content = @Content(schema = @Schema(implementation = GenerationDetailResponse.class),
                examples = {
                    @ExampleObject(name = "완료 — 사진 가이드 체크", description = "본문에 사진 안내 태그가 섞여 있습니다",
                        value = """
                            {
                              "generationId": 1,
                              "status": "COMPLETED",
                              "purpose": "NEW_MENU_PROMOTION",
                              "tone": "EMOTIONAL",
                              "keywords": ["밤라떼", "가을신메뉴"],
                              "results": [
                                {
                                  "channel": "BLOG",
                                  "status": "SUCCEEDED",
                                  "message": "생성에 성공했습니다",
                                  "title": "가을의 온기를 담은 신메뉴, 포근한 밤라떼",
                                  "body": "어느덧 바람 끝에 서늘한 기운이 묻어나는 계절입니다. <photo-guide type=\\"MOOD\\" title=\\"가을 감성 가득한 매장\\" description=\\"창가 자리의 햇살을 배경으로 촬영하세요\\"/> 이번에 선보이는 밤라떼는 잘 익은 밤의 고소한 풍미를 담았습니다. <photo-guide type=\\"MENU\\" title=\\"따뜻한 밤라떼 근접샷\\" description=\\"거품과 토핑이 잘 보이도록 가까이서 찍어 주세요\\"/> 창밖 풍경과 함께 즐겨 보세요.",
                                  "hashtags": ["#가을신메뉴", "#밤라떼", "#가을감성"]
                                },
                                {
                                  "channel": "INSTAGRAM",
                                  "status": "SUCCEEDED",
                                  "message": "생성에 성공했습니다",
                                  "title": null,
                                  "body": "성큼 다가온 가을, 마음까지 녹여 줄 한 잔을 준비했어요. <photo-guide type=\\"MENU\\" title=\\"밤라떼 메뉴 사진\\" description=\\"김이 올라오는 잔을 클로즈업해 주세요\\"/> 오늘 하루, 포근한 온기를 더해 보세요.",
                                  "hashtags": ["#가을신메뉴", "#밤라떼", "#카페스타그램"]
                                }
                              ]
                            }
                            """),
                    @ExampleObject(name = "완료 — 사진 가이드 미체크·부분 실패",
                        description = "본문에 태그가 없고, 실패한 채널은 본문 없이 문구만 옵니다",
                        value = """
                            {
                              "generationId": 2,
                              "status": "COMPLETED",
                              "purpose": "EVENT_DISCOUNT",
                              "tone": "CASUAL",
                              "keywords": [],
                              "results": [
                                {
                                  "channel": "BLOG",
                                  "status": "SUCCEEDED",
                                  "message": "생성에 성공했습니다",
                                  "title": "이번 주말, 아메리카노 1+1 이벤트",
                                  "body": "이번 주말 매장에서 아메리카노 1+1 이벤트를 진행합니다. 소중한 사람과 함께 들러 주세요.",
                                  "hashtags": ["#주말이벤트", "#아메리카노"]
                                },
                                {
                                  "channel": "INSTAGRAM",
                                  "status": "FAILED",
                                  "message": "요청이 많습니다. 잠시 후 다시 시도해 주세요",
                                  "title": null,
                                  "body": null,
                                  "hashtags": []
                                }
                              ]
                            }
                            """),
                    @ExampleObject(name = "생성 중", description = "아직 결과가 확정되지 않은 채널은 PENDING 입니다",
                        value = """
                            {
                              "generationId": 3,
                              "status": "IN_PROGRESS",
                              "purpose": "NEW_MENU_PROMOTION",
                              "tone": "EMOTIONAL",
                              "keywords": ["밤라떼"],
                              "results": [
                                {
                                  "channel": "BLOG",
                                  "status": "PENDING",
                                  "message": "생성 중입니다",
                                  "title": null,
                                  "body": null,
                                  "hashtags": []
                                },
                                {
                                  "channel": "INSTAGRAM",
                                  "status": "PENDING",
                                  "message": "생성 중입니다",
                                  "title": null,
                                  "body": null,
                                  "hashtags": []
                                }
                              ]
                            }
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
        @ApiResponse(responseCode = "404", description = "작업이 없거나 본인의 작업이 아닙니다 (CT0002)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"CT0002","message":"생성 작업을 찾을 수 없습니다"}
                    """)))
    })
    GenerationDetailResponse getById(Long memberId, Long generationId);
}
