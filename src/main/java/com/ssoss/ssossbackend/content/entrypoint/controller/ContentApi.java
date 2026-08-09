package com.ssoss.ssossbackend.content.entrypoint.controller;

import com.ssoss.ssossbackend.content.entrypoint.request.ContentChannelEditRequest;
import com.ssoss.ssossbackend.content.entrypoint.request.ContentListRequest;
import com.ssoss.ssossbackend.content.entrypoint.request.ContentRenameRequest;
import com.ssoss.ssossbackend.content.entrypoint.request.ContentSaveRequest;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentChannelResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentDetailResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentListResponse;
import com.ssoss.ssossbackend.content.entrypoint.response.ContentSaveResponse;
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

@Tag(name = "콘텐츠")
interface ContentApi {

    @Operation(
        summary = "저장하기",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            생성 작업의 결과를 콘텐츠로 저장합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 본인의 작업만 저장할 수 있습니다.
            - 저장 단위는 작업 하나입니다. 작업 id 와 함께 그 작업의 채널을 빠짐없이 담아 보내면 콘텐츠 1건이 됩니다.
              채널을 골라 저장하는 방법은 없습니다.
            - 제목·본문·해시태그는 화면에 보이는 값을 그대로 보냅니다. 손대지 않았다면 생성 작업 조회의 results 원소를 그대로 옮기면 되고,
              화면에서 고쳤다면 고친 값이 저장됩니다. 서버가 생성 결과를 다시 읽어 덮어쓰지 않습니다.
            - 채널 구성이 작업과 다르면 400(CT0010)입니다. 채널을 빠뜨리거나, 작업에 없는 채널을 넣거나, 같은 채널을 두 번 보내거나,
              contents 를 아예 보내지 않으면 아무것도 저장되지 않습니다.
              채널 이름 자체가 없는 값이면(오타 등) 400(C0001)이라 CT0010 과 구분됩니다.
            - 제목은 채널에 맞춰 보냅니다. 블로그는 필수라 빠지면 400(CT0007)이고,
              나머지 채널(인스타그램·당근 비즈·스레드)은 제목을 쓸 수 없어 보내면 400(CT0008)입니다. 편집 API 와 같은 규칙입니다.
              빈 문자열과 공백만 있는 제목은 제목을 보내지 않은 것으로 봅니다.
            - 상한도 편집과 같은 제목 60자·해시태그 20개입니다. 넘으면 400(C0001)이며 어떤 값도 저장되지 않습니다.
            - 성공한 작업만 저장할 수 있습니다. 조회 API 의 status 가 SUCCEEDED 인 작업을 보내세요.
              아직 IN_PROGRESS 면 409 로, FAILED 면 400 으로 거부됩니다.
            - 같은 작업을 다시 저장해도 콘텐츠가 늘어나지 않습니다. 이미 저장된 채널은 새로 보낸 값으로 덮어쓰지 않고 그대로 두며,
              contentId 와 contentChannelId 를 처음 저장할 때와 같은 값으로 반환합니다. 저장한 값을 고칠 때는 편집 API 를 쓰세요.
            - 저장한 뒤 삭제한 작업은 다시 저장할 수 없습니다. 409(CT0009)로 거부하며 삭제한 콘텐츠는 되살아나지 않습니다.
              삭제 후에는 같은 작업으로 저장하기를 다시 호출하지 말고, 필요하면 새로 생성해 주세요.
            - 반환된 contentId 로 상세 조회에, contents[].contentChannelId 로 편집·삭제·다른 채널용으로 만들기·스타일 재사용에 들어갑니다.
            - contents 는 블로그 → 인스타그램 → 당근 비즈 → 스레드 순으로 담깁니다. 요청한 채널 순서나 저장된 순서와 무관하게 항상 같습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "저장되었습니다. 이미 저장된 작업이면 기존 콘텐츠를 그대로 반환합니다",
            content = @Content(schema = @Schema(implementation = ContentSaveResponse.class),
                examples = @ExampleObject(name = "2채널 저장", value = """
                    {
                      "contentId": 1,
                      "contents": [
                        {"contentChannelId": 10, "channel": "BLOG"},
                        {"contentChannelId": 11, "channel": "INSTAGRAM"}
                      ]
                    }
                    """))),
        @ApiResponse(responseCode = "400",
            description = "입력값이 잘못되었거나 (C0001) 채널 구성이 작업과 다르거나 (CT0010) "
                + "채널과 제목이 어긋나거나 (CT0007·CT0008) 생성에 실패한 작업입니다 (CT0003)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "입력값 오류", value = """
                        {"code":"C0001","message":"생성 작업 id 를 입력해 주세요"}
                        """),
                    @ExampleObject(name = "채널 구성 불일치", value = """
                        {"code":"CT0010","message":"저장할 채널이 생성 작업의 채널과 다릅니다"}
                        """),
                    @ExampleObject(name = "블로그인데 제목 없음", value = """
                        {"code":"CT0007","message":"제목을 입력해 주세요"}
                        """),
                    @ExampleObject(name = "제목 없는 채널에 제목", value = """
                        {"code":"CT0008","message":"제목을 쓸 수 없는 채널입니다"}
                        """),
                    @ExampleObject(name = "실패한 작업", value = """
                        {"code":"CT0003","message":"생성에 실패한 작업은 저장할 수 없습니다"}
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
                    """))),
        @ApiResponse(responseCode = "409",
            description = "작업이 아직 진행 중이거나 (CT0004) 저장했다 삭제한 작업입니다 (CT0009)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "진행 중인 작업",
                        description = "조회 API 의 status 가 IN_PROGRESS 가 아니게 된 뒤에 저장해 주세요", value = """
                        {"code":"CT0004","message":"생성이 아직 끝나지 않았습니다. 끝난 뒤 저장해 주세요"}
                        """),
                    @ExampleObject(name = "삭제한 콘텐츠", value = """
                        {"code":"CT0009","message":"이미 삭제한 콘텐츠는 다시 저장할 수 없습니다"}
                        """)}))
    })
    ResponseEntity<ContentSaveResponse> save(Long memberId, ContentSaveRequest request);

    @Operation(
        summary = "생성 기록 목록 조회",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            저장한 콘텐츠를 저장 시각순으로 조회합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 본인의 콘텐츠만 조회됩니다.
            - 카드 1건은 저장하기 1회로 만들어진 콘텐츠 하나입니다. 3채널을 저장했어도 카드는 1건이고 channels 에 채널 셋이 담깁니다.
            - channels 는 블로그 → 인스타그램 → 당근 비즈 → 스레드 순으로 담깁니다. 저장·상세 응답과 같은 규칙입니다.
            - 삭제한 콘텐츠는 목록에도 totalCount 에도 들어가지 않습니다.
            - 정렬 기준은 저장 시각이고 sort 로 방향만 고릅니다. LATEST 는 최신순, OLDEST 는 오래된 순이며 생략하면 LATEST 입니다.
              편집해도 저장 시각은 움직이지 않아 순서가 흔들리지 않습니다.
            - page 는 0 부터 세고 size 는 기본 20·최대 50 입니다. 홈의 "최근 생성된 콘텐츠"는 size=3 으로 부르면 됩니다.
            - title 은 channels 의 첫 채널에서 가져옵니다. 채널마다 제목이 다르지만 카드에는 하나만 보이기 때문입니다.
            - name 은 저장할 때 같은 규칙으로 뽑아 굳혀 둔 값입니다. 그래서 채널을 편집하면 title 만 따라 바뀌고 name 은 그대로입니다.
            - hashtags 는 channels 에서 해시태그가 있는 첫 채널의 앞 2개입니다. 어느 채널에도 없으면 빈 배열입니다.
              당근 비즈는 해시태그를 만들지 않는 채널이라, 첫 채널에서만 가져오면 당근 비즈가 앞선 카드는 태그가 비어 버리기 때문입니다.
            - title 은 말줄임표를 포함해 20자를 넘지 않습니다. 넘치면 서버가 19자에서 자르고 말줄임표(…)를 붙이므로 화면에서 다시 자르지 않아도 됩니다.
            - channel 로 거르면 그 채널을 포함한 저장 건만 남고, 남은 카드의 channels 는 자르지 않고 전부 보여줍니다.
            - 본문은 담기지 않습니다. 카드를 눌러 들어갈 때 상세 조회로 받으세요.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "고른 정렬 방향의 카드 목록과 전체 건수를 반환합니다",
            content = @Content(schema = @Schema(implementation = ContentListResponse.class),
                examples = @ExampleObject(name = "2건", description = "두 번째 카드는 제목 없는 채널이라 본문에서 제목을 채웠고, 둘 다 20자를 넘어 잘렸습니다",
                    value = """
                        {
                          "totalCount": 2,
                          "page": 0,
                          "size": 20,
                          "hasNext": false,
                          "contents": [
                            {
                              "contentId": 2,
                              "name": "을지로 크루아상 맛집 | 겹겹이 살…",
                              "savedAt": "2026-09-01T09:41:00Z",
                              "channels": ["BLOG", "THREADS"],
                              "purpose": "INFORMATION",
                              "tone": "CASUAL",
                              "title": "을지로 크루아상 맛집 | 겹겹이 살…",
                              "hashtags": ["#을지로카페", "#을지로크루아상"]
                            },
                            {
                              "contentId": 1,
                              "name": "성큼 다가온 가을, 마음까지 녹여줄…",
                              "savedAt": "2026-08-23T02:10:00Z",
                              "channels": ["INSTAGRAM"],
                              "purpose": "NEW_MENU_PROMOTION",
                              "tone": "EMOTIONAL",
                              "title": "성큼 다가온 가을, 마음까지 녹여줄…",
                              "hashtags": ["#가을신메뉴", "#카페스타그램"]
                            }
                          ]
                        }
                        """))),
        @ApiResponse(responseCode = "400", description = "channel·sort 가 없는 값이거나 page·size 가 허용 범위를 벗어났습니다 (C0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "size 상한 초과", value = """
                        {"code":"C0001","message":"한 번에 최대 50건까지 조회할 수 있습니다"}
                        """),
                    @ExampleObject(name = "없는 채널·정렬", value = """
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
    ContentListResponse list(Long memberId, ContentListRequest request);

    @Operation(
        summary = "콘텐츠 상세 조회",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            저장한 콘텐츠 1건을 채널별 본문까지 통째로 조회합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 본인의 콘텐츠만 조회할 수 있습니다.
            - 콘텐츠 1건은 저장하기 1회로 만들어지는 묶음입니다. 3채널을 저장했다면 콘텐츠는 1건이고 그 안에 채널 셋이 담깁니다.
              생성 기록 카드 하나가 곧 콘텐츠 하나라, 카드를 눌러 들어온 상세 화면의 채널 탭을 한 번의 호출로 그릴 수 있습니다.
            - contents 는 블로그 → 인스타그램 → 당근 비즈 → 스레드 순으로 담깁니다. 저장 응답과 같은 규칙입니다.
            - 저장된 최신본이 옵니다. 편집했다면 편집한 내용이, 편집하지 않았다면 저장할 때 복사해 둔 내용이 그대로 옵니다.
            - 제목은 저장된 값을 그대로 돌려줍니다. 제목 없는 채널(인스타그램·당근 비즈·스레드)은 title 이 null 입니다.
              목록처럼 서버가 본문에서 제목을 만들어 채우거나 20자에서 자르지 않습니다.
            - name 은 목록 카드에서 본 이름과 같은 값입니다. 카드에서 상세로 들어와도 부르던 이름이 그대로 이어집니다.
            - purpose·tone·keywords 는 콘텐츠 전체의 생성 조건입니다. 화면 상단의 "정보성 · 일상형"과 활용 키워드가 이 값들이며, 채널이 달라도 같습니다.
              저장할 때 복사해 두므로 원본 생성 작업이 없어도 그대로 옵니다.
            - 편집은 이 콘텐츠의 contentId 와 채널마다 다른 contents[].contentChannelId 를 함께 써서 호출합니다.
              삭제·다른 채널용으로 만들기·스타일 재사용도 여기서 얻은 id 를 씁니다.
            - 사진 가이드 태그가 섞인 본문은 생성 작업 조회와 같은 형식이라 같은 파서를 쓸 수 있습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "콘텐츠의 이름·목적·톤·키워드와 채널별 제목·본문·해시태그를 반환합니다",
            content = @Content(schema = @Schema(implementation = ContentDetailResponse.class),
                examples = {
                    @ExampleObject(name = "2채널 저장", description = "블로그는 제목이 있고 스레드는 없습니다", value = """
                        {
                          "contentId": 1,
                          "name": "을지로 크루아상 맛집 | 겹겹이 살…",
                          "purpose": "INFORMATION",
                          "tone": "CASUAL",
                          "keywords": ["디저트", "크루아상", "을지로베이커리"],
                          "contents": [
                            {
                              "contentChannelId": 10,
                              "channel": "BLOG",
                              "title": "을지로 크루아상 맛집 | 겹겹이 살아있는 결, 보니스커피",
                              "body": "을지로에서 크루아상 하나를 제대로 먹고 싶다면, 보니스커피를 추천드려요...",
                              "hashtags": ["#을지로카페", "#을지로크루아상", "#보니스커피"]
                            },
                            {
                              "contentChannelId": 11,
                              "channel": "THREADS",
                              "title": null,
                              "body": "매일 아침 6시부터 미는 반죽, 결이 살아있는 크루아상 한 입 어떠세요?",
                              "hashtags": ["#을지로카페", "#크루아상맛집"]
                            }
                          ]
                        }
                        """),
                    @ExampleObject(name = "1채널 저장 — 키워드 없음", value = """
                        {
                          "contentId": 2,
                          "name": "가을 신메뉴 안내",
                          "purpose": "NEW_MENU_PROMOTION",
                          "tone": "EMOTIONAL",
                          "keywords": [],
                          "contents": [
                            {
                              "contentChannelId": 12,
                              "channel": "INSTAGRAM",
                              "title": null,
                              "body": "성큼 다가온 가을, 마음까지 녹여 줄 한 잔을 준비했어요.",
                              "hashtags": ["#가을신메뉴", "#카페스타그램"]
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
        @ApiResponse(responseCode = "404", description = "콘텐츠가 없거나 본인의 콘텐츠가 아닙니다 (CT0005)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"CT0005","message":"콘텐츠를 찾을 수 없습니다"}
                    """)))
    })
    ContentDetailResponse getById(Long memberId, Long contentId);

    @Operation(
        summary = "채널별 콘텐츠 편집",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            저장한 콘텐츠의 채널 하나를 편집합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 본인의 콘텐츠만 편집할 수 있습니다.
            - 편집 단위는 채널입니다. 3채널을 저장했다면 채널마다 따로 호출하며, 편집하지 않은 채널은 그대로 남습니다.
              삭제는 콘텐츠 통째로 하는 것과 단위가 다릅니다.
            - 제목·본문·해시태그를 통째로 받아 교체합니다. 일부만 바꾸더라도 세 값을 모두 보내세요.
              해시태그를 전부 지우려면 빈 배열을 보냅니다.
            - 제목은 채널에 맞춰 보냅니다. 블로그는 필수라 빠지면 400(CT0007)이고,
              나머지 채널(인스타그램·당근 비즈·스레드)은 제목을 쓸 수 없어 보내면 400(CT0008)입니다.
              빈 문자열과 공백만 있는 값은 제목을 보내지 않은 것으로 봅니다. 입력칸을 비운 채로 보내도 400 이 아니라 제목 없음으로 처리됩니다.
            - 상한은 제목 60자, 해시태그 20개입니다. 넘으면 400(C0001)이며 어떤 값도 바뀌지 않습니다.
            - 편집해도 저장 시각은 움직이지 않습니다. 생성 기록 목록의 정렬 순서가 편집 때문에 흔들리지 않습니다.
            - 세 값이 모두 그대로면 아무것도 바꾸지 않고 현재 값을 그대로 반환합니다. 같은 요청을 여러 번 보내도 결과가 같습니다.
            - 응답은 상세 조회의 contents 원소와 같은 형식이라, 편집한 채널 탭만 응답으로 갈아 끼우면 됩니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "편집된 채널별 콘텐츠를 반환합니다",
            content = @Content(schema = @Schema(implementation = ContentChannelResponse.class),
                examples = @ExampleObject(name = "블로그 편집", value = """
                    {
                      "contentChannelId": 10,
                      "channel": "BLOG",
                      "title": "을지로 크루아상 맛집 | 겹겹이 살아있는 결, 보니스커피",
                      "body": "을지로에서 크루아상 하나를 제대로 먹고 싶다면, 보니스커피를 추천드려요...",
                      "hashtags": ["#을지로카페", "#을지로크루아상"]
                    }
                    """))),
        @ApiResponse(responseCode = "400",
            description = "상한을 넘었거나 (C0001) 채널과 제목이 어긋납니다 (CT0007·CT0008)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "제목 상한 초과", value = """
                        {"code":"C0001","message":"제목은 60자 이내로 입력해 주세요"}
                        """),
                    @ExampleObject(name = "해시태그 상한 초과", value = """
                        {"code":"C0001","message":"해시태그는 최대 20개까지 입력할 수 있습니다"}
                        """),
                    @ExampleObject(name = "블로그인데 제목 없음", value = """
                        {"code":"CT0007","message":"제목을 입력해 주세요"}
                        """),
                    @ExampleObject(name = "제목 없는 채널에 제목", value = """
                        {"code":"CT0008","message":"제목을 쓸 수 없는 채널입니다"}
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
            description = "콘텐츠가 없거나 본인의 콘텐츠가 아니거나 (CT0005) 그 콘텐츠에 없는 채널입니다 (CT0006)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "콘텐츠 없음", value = """
                        {"code":"CT0005","message":"콘텐츠를 찾을 수 없습니다"}
                        """),
                    @ExampleObject(name = "채널별 콘텐츠 없음", value = """
                        {"code":"CT0006","message":"채널별 콘텐츠를 찾을 수 없습니다"}
                        """)}))
    })
    ContentChannelResponse edit(Long memberId, Long contentId, Long contentChannelId,
        ContentChannelEditRequest request);

    @Operation(
        summary = "콘텐츠 이름 수정",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            저장한 콘텐츠의 이름을 바꿉니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 본인의 콘텐츠만 바꿀 수 있습니다.
            - 이름은 콘텐츠 단위라 채널마다 다르지 않습니다. 채널별 제목을 바꾸는 편집과 대상이 다릅니다.
            - 저장할 때는 서버가 대표 채널에서 이름을 뽑아 두고, 이 API 로 회원이 원하는 이름으로 바꿉니다.
              바꾼 이름은 목록 카드와 상세에 함께 반영되며, 그 뒤에 채널을 편집해도 이름은 그대로입니다.
            - 상한은 20자입니다. 목록 카드 한 줄에 들어갈 길이라, 서버가 뽑아 두는 이름도 말줄임표까지 세어 20자를 넘지 않습니다.
              넘기거나 빈 이름을 보내면 400(C0001)이며 이름은 바뀌지 않습니다.
            - 이름을 바꿔도 저장 시각은 움직이지 않아 생성 기록 목록의 정렬 순서가 흔들리지 않습니다.
            - 응답은 상세 조회와 같은 형식입니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이름을 바꾼 콘텐츠를 상세 조회와 같은 형식으로 반환합니다",
            content = @Content(schema = @Schema(implementation = ContentDetailResponse.class),
                examples = @ExampleObject(name = "이름 수정", value = """
                    {
                      "contentId": 1,
                      "name": "9월 신메뉴 안내",
                      "purpose": "INFORMATION",
                      "tone": "CASUAL",
                      "keywords": ["디저트", "크루아상", "을지로베이커리"],
                      "contents": [
                        {
                          "contentChannelId": 10,
                          "channel": "BLOG",
                          "title": "을지로 크루아상 맛집 | 겹겹이 살아있는 결, 보니스커피",
                          "body": "을지로에서 크루아상 하나를 제대로 먹고 싶다면, 보니스커피를 추천드려요...",
                          "hashtags": ["#을지로카페", "#을지로크루아상", "#보니스커피"]
                        }
                      ]
                    }
                    """))),
        @ApiResponse(responseCode = "400", description = "이름이 비었거나 상한을 넘었습니다 (C0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "빈 이름", value = """
                        {"code":"C0001","message":"이름을 입력해 주세요"}
                        """),
                    @ExampleObject(name = "상한 초과", value = """
                        {"code":"C0001","message":"이름은 20자 이내로 입력해 주세요"}
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
        @ApiResponse(responseCode = "404", description = "콘텐츠가 없거나 본인의 콘텐츠가 아닙니다 (CT0005)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"CT0005","message":"콘텐츠를 찾을 수 없습니다"}
                    """)))
    })
    ContentDetailResponse rename(Long memberId, Long contentId, ContentRenameRequest request);

    @Operation(
        summary = "저장 콘텐츠 삭제",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            저장한 콘텐츠 1건을 삭제합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 이며, 본인의 콘텐츠만 삭제할 수 있습니다.
            - 삭제 단위는 콘텐츠 통째입니다. 3채널을 저장했다면 세 채널이 함께 사라지며, 채널 하나만 골라 지우는 방법은 없습니다.
              채널마다 따로 호출하는 편집과 단위가 다릅니다.
            - 되돌릴 수 없습니다. 복구 API 가 없고, 같은 작업을 다시 저장해도 되살아나지 않습니다(409 CT0009).
            - 삭제한 콘텐츠는 목록·상세에서 사라지고, 상세·편집·삭제를 다시 호출하면 404(CT0005)입니다.
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
        @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없습니다 (CT0005)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = """
                    {"code":"CT0005","message":"콘텐츠를 찾을 수 없습니다"}
                    """)))
    })
    void delete(Long memberId, Long contentId);
}
