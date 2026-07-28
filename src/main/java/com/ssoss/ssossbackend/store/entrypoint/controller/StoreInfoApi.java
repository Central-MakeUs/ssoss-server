package com.ssoss.ssossbackend.store.entrypoint.controller;

import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
import com.ssoss.ssossbackend.store.entrypoint.request.StoreBasicInfoRequest;
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
            - status 는 그룹의 네 가지 중 값이 있는 것이 몇 개인지로 갈립니다 — 하나도 없으면 NOT_WRITTEN, 일부만 있으면 IN_PROGRESS,
              넷 다 있으면 COMPLETED 입니다. 운영 정보의 영업 시각은 시작·종료가 둘 다 있어야, 편의 시설은 포장·예약·주차 중 하나라도 가능이어야
              값이 있는 것으로 셉니다.
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
                            "introduction": null, "status": "IN_PROGRESS"},
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
              입력칸을 비운 채로 보내도 400 이 아니라 소개 없음으로 저장되며, 작성 상태도 그렇게 셉니다.
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
}
