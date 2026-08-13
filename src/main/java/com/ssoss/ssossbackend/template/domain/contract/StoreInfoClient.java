package com.ssoss.ssossbackend.template.domain.contract;

import com.ssoss.ssossbackend.template.domain.model.StoreInfo;

public interface StoreInfoClient {

    StoreInfo get(Long memberId);
}
