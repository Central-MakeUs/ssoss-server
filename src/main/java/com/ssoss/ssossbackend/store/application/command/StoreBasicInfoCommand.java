package com.ssoss.ssossbackend.store.application.command;

import com.ssoss.ssossbackend.store.domain.model.StoreType;

public record StoreBasicInfoCommand(
    Long memberId,
    String name,
    StoreType type,
    String address,
    String introduction
) {

    public static StoreBasicInfoCommand of(Long memberId, String name, String type, String address,
        String introduction) {
        return new StoreBasicInfoCommand(memberId, name, StoreType.from(type), address, introduction);
    }
}
