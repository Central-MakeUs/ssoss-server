package com.ssoss.ssossbackend.app.application.service;

import com.ssoss.ssossbackend.app.domain.model.AppErrorCode;
import com.ssoss.ssossbackend.app.domain.model.AppOs;
import com.ssoss.ssossbackend.app.domain.model.AppVersion;
import com.ssoss.ssossbackend.app.domain.model.SemanticVersion;
import com.ssoss.ssossbackend.app.domain.service.AppVersionFinder;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppVersionService {

    private final AppVersionFinder appVersionFinder;

    public AppVersionResult check(String os, String version) {
        AppVersion appVersion = appVersionFinder.find(AppOs.from(os))
            .orElseThrow(() -> new BusinessException(AppErrorCode.APP_VERSION_NOT_FOUND));
        boolean updateRequired = appVersion.requiresUpdateFrom(SemanticVersion.from(version));
        return new AppVersionResult(updateRequired, appVersion.getMinimumVersion());
    }
}
