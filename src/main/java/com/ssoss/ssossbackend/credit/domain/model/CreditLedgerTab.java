package com.ssoss.ssossbackend.credit.domain.model;

import java.util.Arrays;
import java.util.List;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;

public enum CreditLedgerTab {

    USE(List.of(CreditLedgerType.DEDUCT)),
    GAIN(List.of(CreditLedgerType.GRANT, CreditLedgerType.CHARGE));

    private static final List<CreditLedgerType> LISTED_TYPES = Arrays.stream(values())
        .flatMap(tab -> tab.types.stream())
        .distinct()
        .toList();

    private final List<CreditLedgerType> types;

    CreditLedgerTab(List<CreditLedgerType> types) {
        this.types = types;
    }

    public static CreditLedgerTab from(String value) {
        return Arrays.stream(values())
            .filter(tab -> tab.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_INPUT));
    }

    public static List<CreditLedgerType> typesOf(CreditLedgerTab tab) {
        return tab == null ? LISTED_TYPES : tab.types;
    }
}
