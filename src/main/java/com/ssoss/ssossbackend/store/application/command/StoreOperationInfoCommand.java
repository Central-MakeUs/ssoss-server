package com.ssoss.ssossbackend.store.application.command;

import java.util.List;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;
import com.ssoss.ssossbackend.store.domain.model.BusinessDays;
import com.ssoss.ssossbackend.store.domain.model.SignatureMenus;

public record StoreOperationInfoCommand(
    Long memberId,
    BusinessDays businessDays,
    String openTime,
    String closeTime,
    SignatureMenus signatureMenus,
    boolean takeoutAvailable,
    boolean reservationAvailable,
    boolean parkingAvailable
) {

    public static StoreOperationInfoCommand of(Long memberId, List<String> businessDays, String openTime,
        String closeTime, List<String> signatureMenus, Boolean takeoutAvailable, Boolean reservationAvailable,
        Boolean parkingAvailable) {
        if ((openTime == null) != (closeTime == null)) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT);
        }
        return new StoreOperationInfoCommand(
            memberId,
            BusinessDays.from(businessDays),
            openTime,
            closeTime,
            new SignatureMenus(signatureMenus),
            Boolean.TRUE.equals(takeoutAvailable),
            Boolean.TRUE.equals(reservationAvailable),
            Boolean.TRUE.equals(parkingAvailable));
    }
}
