package com.ssoss.ssossbackend.credit.domain.model;

import java.util.Arrays;
import java.util.List;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;

public enum CreditLedgerTab {

    ALL,
    USE(CreditLedgerType.DEDUCT),
    GAIN(CreditLedgerType.GRANT, CreditLedgerType.CHARGE);

    private static final List<CreditLedgerType> EVERY_TAB_TYPES = Arrays.stream(values())
        .flatMap(tab -> tab.types.stream())
        .distinct()
        .toList();

    private final List<CreditLedgerType> types;

    CreditLedgerTab(CreditLedgerType... types) {
        this.types = List.of(types);
    }

    public static CreditLedgerTab from(String value) {
        return Arrays.stream(values())
            .filter(tab -> tab.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_INPUT));
    }

    public List<CreditLedgerType> types() {
        return types.isEmpty() ? EVERY_TAB_TYPES : types;
    }
}
