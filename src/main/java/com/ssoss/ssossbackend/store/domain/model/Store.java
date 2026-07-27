package com.ssoss.ssossbackend.store.domain.model;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("store")
public class Store {

    @Id
    private Long id;
    private Long memberId;
    private String name;
    private StoreType type;
    private String address;
    private String introduction;
    private BusinessDays businessDays;
    private String openTime;
    private String closeTime;
    private SignatureMenus signatureMenus;
    private Boolean takeoutAvailable;
    private Boolean reservationAvailable;
    private Boolean parkingAvailable;
    private String strength;
    private StoreKeywords keywords;
    private String forbidden;
    private Tone tone;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    Store(Long id, Long memberId, String name, StoreType type, String address, String introduction,
        BusinessDays businessDays, String openTime, String closeTime, SignatureMenus signatureMenus,
        Boolean takeoutAvailable, Boolean reservationAvailable, Boolean parkingAvailable,
        String strength, StoreKeywords keywords, String forbidden, Tone tone) {
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.type = type;
        this.address = address;
        this.introduction = introduction;
        this.businessDays = businessDays;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.signatureMenus = signatureMenus;
        this.takeoutAvailable = takeoutAvailable;
        this.reservationAvailable = reservationAvailable;
        this.parkingAvailable = parkingAvailable;
        this.strength = strength;
        this.keywords = keywords;
        this.forbidden = forbidden;
        this.tone = tone;
    }

    public static Store create(Long memberId) {
        return new Store(null, memberId, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null);
    }
}
