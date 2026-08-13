package com.ssoss.ssossbackend.content.domain.contract;

import com.ssoss.ssossbackend.content.domain.model.StoreMaterial;

public interface StoreMaterialClient {

    StoreMaterial get(Long memberId);
}
