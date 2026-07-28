package com.ssoss.ssossbackend.store.domain.model;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.StringUtils;

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

    @Embedded.Empty
    private Amenities amenities;

    private String strength;
    private StoreKeywords keywords;
    private String forbidden;
    private Tone tone;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    Store(Long id, Long memberId, String name, StoreType type, String address, String introduction,
        BusinessDays businessDays, String openTime, String closeTime, SignatureMenus signatureMenus,
        Amenities amenities, String strength, StoreKeywords keywords, String forbidden, Tone tone) {
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
        this.amenities = amenities;
        this.strength = strength;
        this.keywords = keywords;
        this.forbidden = forbidden;
        this.tone = tone;
    }

    public static Store create(Long memberId) {
        return new Store(null, memberId, null, null, null, null, null, null, null, null,
            new Amenities(false, false, false), null, null, null, null);
    }

    public void writeBasicInfo(String name, StoreType type, String address, String introduction) {
        this.name = name;
        this.type = type;
        this.address = address;
        this.introduction = StringUtils.hasText(introduction) ? introduction : null;
    }

    public void writeOperationInfo(BusinessDays businessDays, String openTime, String closeTime,
        SignatureMenus signatureMenus, Amenities amenities) {
        this.businessDays = businessDays;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.signatureMenus = signatureMenus;
        this.amenities = amenities;
    }

    public List<DayOfWeek> businessDayValues() {
        return businessDays == null ? List.of() : businessDays.values();
    }

    public List<String> signatureMenuValues() {
        return signatureMenus == null ? List.of() : signatureMenus.values();
    }

    public List<String> keywordValues() {
        return keywords == null ? List.of() : keywords.values();
    }

    public String typeName() {
        return type == null ? null : type.name();
    }

    public String toneName() {
        return tone == null ? null : tone.name();
    }

    public StoreInfoStatus basicInfoStatus() {
        return StoreInfoStatus.of(
            name != null,
            type != null,
            address != null,
            introduction != null);
    }

    public StoreInfoStatus operationInfoStatus() {
        return StoreInfoStatus.of(
            !businessDayValues().isEmpty(),
            openTime != null && closeTime != null,
            !signatureMenuValues().isEmpty(),
            amenities.anyAvailable());
    }

    public StoreInfoStatus contentInfoStatus() {
        return StoreInfoStatus.of(
            strength != null,
            !keywordValues().isEmpty(),
            forbidden != null,
            tone != null);
    }
}
