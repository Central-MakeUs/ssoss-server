package com.ssoss.ssossbackend.store.entrypoint.controller;

import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.store.entrypoint.request.StoreBasicInfoRequest;
import com.ssoss.ssossbackend.store.entrypoint.request.StoreContentInfoRequest;
import com.ssoss.ssossbackend.store.entrypoint.request.StoreOperationInfoRequest;
import com.ssoss.ssossbackend.store.entrypoint.response.StoreInfoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "매장")
interface StoreInfoApi {

    @Operation(
        summary = "매장 정보 조회",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            가입 회원(ACTIVE)이 자기 매장 정보를 조회합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 입니다.
            - 기본 정보·운영 정보·콘텐츠 정보 세 그룹의 값과 그룹별 작성 상태를 한 번에 반환합니다.
              마이페이지 홈은 매장명·매장 유형·작성 상태만 골라 쓰고, 매장 프로필 페이지와 그룹별 편집 화면은 같은 응답을 그대로 재사용합니다.
            - 매장은 회원가입 시점에 만들어지므로 온보딩을 건너뛴 회원도 매장이 없다는 응답 대신
              값이 전부 비고 status 가 셋 다 NOT_WRITTEN 인 응답을 받습니다.
            - status 는 그룹의 네 가지 중 값이 하나도 없으면 NOT_WRITTEN, 하나라도 있으면 COMPLETED 입니다.
              운영 정보의 영업 시각은 시작·종료가 둘 다 있어야, 편의 시설은 포장·예약·주차 중 하나라도 가능이어야 값이 있는 것으로 셉니다.
            - 영업 시각은 24시간 `HH:mm` 문자열이며, 종료가 시작보다 이르면 다음날입니다. 오전·오후 표기는 클라이언트가 만듭니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "매장 정보 세 그룹의 값과 그룹별 작성 상태를 반환합니다",
            content = @Content(schema = @Schema(implementation = StoreInfoResponse.class),
                examples = {
                    @ExampleObject(name = "온보딩을 건너뛴 회원", value = """
                        {
                          "basic": {"name": null, "type": null, "address": null, "introduction": null,
                            "status": "NOT_WRITTEN"},
                          "operation": {"businessDays": [], "openTime": null, "closeTime": null, "signatureMenus": [],
                            "takeoutAvailable": false, "reservationAvailable": false, "parkingAvailable": false,
                            "status": "NOT_WRITTEN"},
                          "content": {"strength": null, "keywords": [], "forbidden": null, "tone": null,
                            "status": "NOT_WRITTEN"}
                        }
                        """),
                    @ExampleObject(name = "기본 정보를 한 줄 소개 없이 채운 회원", value = """
                        {
                          "basic": {"name": "보니스커피", "type": "CAFE", "address": "서울 중구 을지로 100",
                            "introduction": null, "status": "COMPLETED"},
                          "operation": {"businessDays": [], "openTime": null, "closeTime": null, "signatureMenus": [],
                            "takeoutAvailable": false, "reservationAvailable": false, "parkingAvailable": false,
                            "status": "NOT_WRITTEN"},
                          "content": {"strength": null, "keywords": [], "forbidden": null, "tone": null,
                            "status": "NOT_WRITTEN"}
                        }
                        """),
                    @ExampleObject(name = "세 그룹을 다 채운 회원", value = """
                        {
                          "basic": {"name": "보니스커피", "type": "CAFE", "address": "서울 중구 을지로 100",
                            "introduction": "매일 아침 굽는 크루아상이 있는 을지로 카페", "status": "COMPLETED"},
                          "operation": {"businessDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
                            "openTime": "09:00", "closeTime": "22:00", "signatureMenus": ["크루아상", "바닐라 라떼"],
                            "takeoutAvailable": true, "reservationAvailable": false, "parkingAvailable": true,
                            "status": "COMPLETED"},
                          "content": {"strength": "매일 아침 직접 굽는 크루아상과 직접 로스팅한 원두",
                            "keywords": ["디저트", "크루아상", "을지로베이커리"], "forbidden": "최저가, 1위 같은 과장 표현",
                            "tone": "CASUAL", "status": "COMPLETED"}
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
                    """)))
    })
    StoreInfoResponse getInfo(Long memberId);

    @Operation(
        summary = "매장 기본 정보 저장",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            가입 회원(ACTIVE)이 자기 매장의 기본 정보를 저장합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 입니다.
            - 온보딩 화면과 마이페이지 기본 정보 화면이 이 API 하나를 함께 씁니다.
              온보딩에서 건너뛰기는 이 API 를 호출하지 않는 것이며, 건너뛴 회원은 기본 정보가 빈 채로 남습니다.
            - 기본 정보 그룹을 통째로 교체합니다. 네 필드를 모두 보내며, 보내지 않은 필드는 비워집니다.
              매장 한 줄 소개만 지우고 싶다면 나머지 셋을 그대로 담고 소개만 빼서 보냅니다.
            - 매장명·매장 유형·주소는 필수라 빠지면 400(C0001)입니다. 매장 한 줄 소개만 선택입니다.
            - 빈 문자열과 공백만 있는 값은 매장 한 줄 소개를 보내지 않은 것으로 봅니다.
              입력칸을 비운 채로 보내도 400 이 아니라 소개 없음으로 저장됩니다.
            - 매장 유형은 카페·디저트 카페·베이커리·베이커리 카페·브런치 카페·로스터리 카페·카페바 일곱 중 하나이며,
              목록에 없는 값은 400(C0001)입니다. "기타"는 없습니다.
            - 상한은 매장명 50자, 주소 200자, 매장 한 줄 소개 100자입니다. 넘으면 400(C0001)이며 어떤 값도 바뀌지 않습니다.
            - 운영 정보·콘텐츠 정보는 이 호출로 바뀌지 않습니다. 그룹마다 저장 API 가 따로 있습니다.
            - 저장 결과는 응답 본문 대신 매장 정보 조회로 확인합니다. 작성 상태도 그 응답에 함께 담깁니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "저장되었습니다"),
        @ApiResponse(responseCode = "400",
            description = "필수 값이 없거나 매장 유형이 목록에 없거나 상한을 넘었습니다 (C0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "매장명 없음", value = """
                        {"code":"C0001","message":"매장명을 입력해 주세요"}
                        """),
                    @ExampleObject(name = "매장 유형 없음", value = """
                        {"code":"C0001","message":"매장 유형을 선택해 주세요"}
                        """),
                    @ExampleObject(name = "주소 없음", value = """
                        {"code":"C0001","message":"주소를 입력해 주세요"}
                        """),
                    @ExampleObject(name = "목록에 없는 매장 유형", value = """
                        {"code":"C0001","message":"입력값을 다시 확인해 주세요"}
                        """),
                    @ExampleObject(name = "매장명 상한 초과", value = """
                        {"code":"C0001","message":"매장명은 50자 이내로 입력해 주세요"}
                        """),
                    @ExampleObject(name = "매장 한 줄 소개 상한 초과", value = """
                        {"code":"C0001","message":"매장 한 줄 소개는 100자 이내로 입력해 주세요"}
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
    void saveBasicInfo(Long memberId, StoreBasicInfoRequest request);

    @Operation(
        summary = "매장 운영 정보 저장",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            가입 회원(ACTIVE)이 자기 매장의 운영 정보를 저장합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 입니다.
            - 온보딩 화면과 마이페이지 운영 정보 화면이 이 API 하나를 함께 씁니다.
              온보딩 화면에는 대표 메뉴 입력란이 없어 그때는 대표 메뉴가 빈 채로 저장되고, 나중에 마이페이지에서 채웁니다.
            - 운영 정보 그룹을 통째로 교체합니다. 보내지 않은 값은 비워지므로, 대표 메뉴를 빼고 다시 저장하면 이전 대표 메뉴가 사라집니다.
            - 네 가지 모두 선택이라 아무것도 보내지 않아도 저장됩니다. 그때는 작성 상태가 미작성이 됩니다.
            - 영업 시간은 요일 집합과 공통 시작·종료 시각 한 쌍입니다. 요일마다 다른 시각을 두지 않습니다.
            - 영업 요일은 MONDAY~SUNDAY 이며 목록에 없는 값은 400(C0001)입니다.
              같은 요일을 여러 번 보내면 한 번만 저장되고 월요일부터의 순서로 정리됩니다.
            - 영업 시각은 24시간 `HH:mm` 문자열이고 형식이 아니면 400(C0001)입니다. 오전·오후 표기는 클라이언트가 만듭니다.
              종료가 시작보다 일러도 다음날로 보고 그대로 저장합니다 — 18시부터 새벽 2시까지 여는 매장이 있어 서버가 검증하지 않습니다.
            - 영업 시각은 시작·종료 한 쌍이라 둘 다 보내거나 둘 다 빼야 하며, 한쪽만 보내면 400(C0001)입니다.
              영업 시각을 지우려면 둘 다 빼고 보냅니다.
            - 대표 메뉴는 최대 10개, 개당 30자입니다. 넘으면 400(C0001)이며 어떤 값도 바뀌지 않습니다.
              빈 문자열이나 공백만 있는 대표 메뉴도 400(C0001)입니다. 대표 메뉴를 비우려면 목록에서 빼고 보냅니다.
            - 편의 시설은 포장·예약·주차 세 가지의 boolean 이며, 보내지 않으면 불가로 저장됩니다.
              미입력과 불가를 구분하지 않습니다.
            - 기본 정보·콘텐츠 정보는 이 호출로 바뀌지 않습니다. 그룹마다 저장 API 가 따로 있습니다.
            - 저장 결과는 응답 본문 대신 매장 정보 조회로 확인합니다. 작성 상태도 그 응답에 함께 담깁니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "저장되었습니다"),
        @ApiResponse(responseCode = "400",
            description = "영업 요일이 목록에 없거나 영업 시각 형식·짝이 맞지 않거나 대표 메뉴 상한을 넘었습니다 (C0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "목록에 없는 영업 요일", value = """
                        {"code":"C0001","message":"입력값을 다시 확인해 주세요"}
                        """),
                    @ExampleObject(name = "영업 시각 형식 오류", value = """
                        {"code":"C0001","message":"영업 시작 시각은 24시간 HH:mm 형식으로 입력해 주세요"}
                        """),
                    @ExampleObject(name = "영업 시각을 한쪽만 보냄", value = """
                        {"code":"C0001","message":"입력값을 다시 확인해 주세요"}
                        """),
                    @ExampleObject(name = "대표 메뉴 개수 초과", value = """
                        {"code":"C0001","message":"대표 메뉴는 최대 10개까지 입력할 수 있습니다"}
                        """),
                    @ExampleObject(name = "대표 메뉴 글자 수 초과", value = """
                        {"code":"C0001","message":"대표 메뉴는 30자 이내로 입력해 주세요"}
                        """),
                    @ExampleObject(name = "빈 대표 메뉴", value = """
                        {"code":"C0001","message":"빈 대표 메뉴는 보낼 수 없습니다"}
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
    void saveOperationInfo(Long memberId, StoreOperationInfoRequest request);

    @Operation(
        summary = "매장 콘텐츠 정보 저장",
        security = @SecurityRequirement(name = "bearerAuth"),
        description = """
            가입 회원(ACTIVE)이 자기 매장의 콘텐츠 정보를 저장합니다.

            - 가입 회원(ACTIVE) accessToken 전용 API 입니다.
            - 콘텐츠 정보는 콘텐츠 생성 화면의 강조 내용·키워드·금지 내용·톤 입력란을 미리 채우는 기본값 묶음입니다.
              서버는 이 값을 저장하고 조회로 돌려줄 뿐 생성 프롬프트에 싣지 않습니다.
              화면에 채우는 일도 결과에 싣는 일도 클라이언트가 하므로, 사용자가 화면에서 지운 값은 결과에도 나오지 않습니다.
            - 그래서 네 필드의 상한은 생성 작업의 강조 내용·키워드·금지 내용·톤과 같습니다.
              값이 있는 필드는 생성 작업에 그대로 옮겨 담아도 400 이 나지 않습니다.
              다만 생성 작업은 강조 내용·톤이 필수라, 여기서 비워 둔 경우에는 화면에서 채워야 합니다.
            - 콘텐츠 정보 그룹을 통째로 교체합니다. 보내지 않은 값은 비워지므로, 키워드를 빼고 다시 저장하면 이전 키워드가 사라집니다.
            - 네 필드 모두 선택이라 아무것도 보내지 않아도 저장됩니다. 그때는 작성 상태가 미작성이 됩니다.
            - 빈 문자열과 공백만 있는 값은 매장 강점·금지 내용·톤을 보내지 않은 것으로 봅니다.
            - 매장 강점·금지 내용은 500자를 넘으면 400(C0001)입니다.
            - 매장 키워드는 최대 10개, 개당 30자입니다. 넘으면 400(C0001)입니다.
              빈 문자열이나 공백만 있는 키워드도 400(C0001)입니다. 키워드를 비우려면 목록에서 빼고 보냅니다.
            - 400 이 나면 네 필드 어떤 값도 바뀌지 않습니다.
            - 매장 키워드는 `#` 없이 보내 주세요. 서버는 받은 그대로 저장하며 `#` 를 붙이지도 떼지도 않습니다.
              해시태그 표기는 클라이언트가 만듭니다.
            - 콘텐츠 작성 톤은 일상형·감성형·정보형·홍보형 넷 중 하나이며, 목록에 없는 값은 400(C0001)입니다.
            - 기본 정보·운영 정보는 이 호출로 바뀌지 않습니다. 그룹마다 저장 API 가 따로 있습니다.
            - 저장 결과는 응답 본문 대신 매장 정보 조회로 확인합니다. 작성 상태도 그 응답에 함께 담깁니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "저장되었습니다"),
        @ApiResponse(responseCode = "400",
            description = "매장 강점·금지 내용이 상한을 넘었거나 키워드 상한을 넘었거나 톤이 목록에 없습니다 (C0001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "매장 강점 상한 초과", value = """
                        {"code":"C0001","message":"매장 강점은 500자 이내로 입력해 주세요"}
                        """),
                    @ExampleObject(name = "금지 내용 상한 초과", value = """
                        {"code":"C0001","message":"금지 내용은 500자 이내로 입력해 주세요"}
                        """),
                    @ExampleObject(name = "매장 키워드 개수 초과", value = """
                        {"code":"C0001","message":"매장 키워드는 최대 10개까지 입력할 수 있습니다"}
                        """),
                    @ExampleObject(name = "매장 키워드 글자 수 초과", value = """
                        {"code":"C0001","message":"매장 키워드는 30자 이내로 입력해 주세요"}
                        """),
                    @ExampleObject(name = "빈 매장 키워드", value = """
                        {"code":"C0001","message":"빈 매장 키워드는 보낼 수 없습니다"}
                        """),
                    @ExampleObject(name = "목록에 없는 톤", value = """
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
    void saveContentInfo(Long memberId, StoreContentInfoRequest request);
}
