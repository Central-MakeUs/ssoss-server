package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.ArrayList;
import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.StoreMaterial;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
class StoreSectionComposer {

    private static final String NO_STORE_SECTION = """
        [매장 정보]
        매장 정보가 제공되지 않았다. 매장명·매장 유형·주소·메뉴 같은 매장에 대한 사실을 지어내지 않는다.""";

    private static final String HEADER = """
        [매장 정보]
        아래는 이 매장에 대한 사실이다. 여기 없는 사실은 지어내지 않는다.""";

    private static final String NAME_LINE = "매장명: %s";
    private static final String TYPE_LINE = "매장 유형: %s";
    private static final String ADDRESS_LINE = "주소: %s";
    private static final String INTRODUCTION_LINE = "한 줄 소개: %s";
    private static final String BUSINESS_DAYS_LINE = "영업일: %s";
    private static final String BUSINESS_HOURS_LINE = "영업 시간: %s~%s";
    private static final String SIGNATURE_MENUS_LINE = "대표 메뉴: %s";
    private static final String AMENITIES_LINE = "편의 시설: %s";

    private static final String LINE_BREAK = "\n";
    private static final String VALUE_SEPARATOR = ", ";

    String compose(StoreMaterial store) {
        if (store.isEmpty()) {
            return NO_STORE_SECTION;
        }
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        if (StringUtils.hasText(store.name())) {
            lines.add(NAME_LINE.formatted(store.name()));
        }
        if (StringUtils.hasText(store.type())) {
            lines.add(TYPE_LINE.formatted(store.type()));
        }
        if (StringUtils.hasText(store.address())) {
            lines.add(ADDRESS_LINE.formatted(store.address()));
        }
        if (StringUtils.hasText(store.introduction())) {
            lines.add(INTRODUCTION_LINE.formatted(store.introduction()));
        }
        if (!store.businessDays().isEmpty()) {
            lines.add(BUSINESS_DAYS_LINE.formatted(String.join(VALUE_SEPARATOR, store.businessDays())));
        }
        if (store.hasBusinessHours()) {
            lines.add(BUSINESS_HOURS_LINE.formatted(store.openTime(), store.closeTime()));
        }
        if (!store.signatureMenus().isEmpty()) {
            lines.add(SIGNATURE_MENUS_LINE.formatted(String.join(VALUE_SEPARATOR, store.signatureMenus())));
        }
        if (!store.amenities().isEmpty()) {
            lines.add(AMENITIES_LINE.formatted(String.join(VALUE_SEPARATOR, store.amenities())));
        }
        return String.join(LINE_BREAK, lines);
    }
}
