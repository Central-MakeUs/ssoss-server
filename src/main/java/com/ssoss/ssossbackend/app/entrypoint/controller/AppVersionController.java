package com.ssoss.ssossbackend.app.entrypoint.controller;

import com.ssoss.ssossbackend.app.application.service.AppVersionResult;
import com.ssoss.ssossbackend.app.application.service.AppVersionService;
import com.ssoss.ssossbackend.app.entrypoint.request.AppVersionCheckRequest;
import com.ssoss.ssossbackend.app.entrypoint.response.AppVersionResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class AppVersionController implements AppVersionApi {

    private final AppVersionService appVersionService;

    @Override
    @GetMapping("/v1/app-versions/{os}")
    public AppVersionResponse check(
        @PathVariable String os,
        @Valid @ParameterObject AppVersionCheckRequest request
    ) {
        AppVersionResult result = appVersionService.check(os, request.version());
        return new AppVersionResponse(result.updateRequired(), result.minimumVersion());
    }
}
