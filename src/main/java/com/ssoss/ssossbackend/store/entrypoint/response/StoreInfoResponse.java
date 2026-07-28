package com.ssoss.ssossbackend.store.entrypoint.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매장 정보 응답 — 기본 정보·운영 정보·콘텐츠 정보 세 그룹의 값과 그룹별 작성 상태")
public record StoreInfoResponse(
    @Schema(description = "기본 정보 — 매장명, 매장 유형, 주소, 매장 한 줄 소개")
    StoreBasicInfoResponse basic,
    @Schema(description = "운영 정보 — 영업 요일, 영업 시각, 대표 메뉴, 편의 시설")
    StoreOperationInfoResponse operation,
    @Schema(description = "콘텐츠 정보 — 매장 강점, 매장 키워드, 금지 내용, 콘텐츠 작성 톤")
    StoreContentInfoResponse content
) {
}
