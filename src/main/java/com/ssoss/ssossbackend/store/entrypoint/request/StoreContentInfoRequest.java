package com.ssoss.ssossbackend.store.entrypoint.request;

import java.util.List;

import com.ssoss.ssossbackend.store.application.command.StoreContentInfoCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "매장 콘텐츠 정보 저장 요청")
public record StoreContentInfoRequest(
    @Schema(description = "매장 강점 (선택) — 생성 작업의 강조 내용을 미리 채우는 값이라 상한이 같습니다",
        example = "매일 아침 직접 굽는 크루아상과 직접 로스팅한 원두", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 500, message = "매장 강점은 500자 이내로 입력해 주세요")
    String strength,
    @Schema(description = "매장 키워드 목록 (선택) — 최대 10개, 개당 30자이며 # 없이 보냅니다",
        example = "[\"디저트\", \"크루아상\", \"을지로베이커리\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 10, message = "매장 키워드는 최대 10개까지 입력할 수 있습니다")
    List<@NotBlank(message = "빈 매장 키워드는 보낼 수 없습니다")
        @Size(max = 30, message = "매장 키워드는 30자 이내로 입력해 주세요") String> keywords,
    @Schema(description = "금지 내용 (선택) — 콘텐츠에 나오면 안 되는 내용입니다", example = "최저가, 1위 같은 과장 표현",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 500, message = "금지 내용은 500자 이내로 입력해 주세요")
    String forbidden,
    @Schema(description = "콘텐츠 작성 톤 (선택) — CASUAL: 일상형, EMOTIONAL: 감성형, INFORMATIVE: 정보형, "
        + "PROMOTIONAL: 홍보형",
        allowableValues = {"CASUAL", "EMOTIONAL", "INFORMATIVE", "PROMOTIONAL"}, example = "CASUAL",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String tone
) {

    public StoreContentInfoCommand toCommand(Long memberId) {
        return StoreContentInfoCommand.of(memberId, strength, keywords, forbidden, tone);
    }
}
