package com.ssoss.ssossbackend.store.entrypoint.controller;

import com.ssoss.ssossbackend.shared.exception.ErrorResponse;
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
}
