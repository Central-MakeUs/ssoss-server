package com.ssoss.ssossbackend.app.domain.service;

import java.util.Optional;

import com.ssoss.ssossbackend.app.domain.contract.AppVersionRepository;
import com.ssoss.ssossbackend.app.domain.model.AppOs;
import com.ssoss.ssossbackend.app.domain.model.AppVersion;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppVersionFinder {

    private final AppVersionRepository appVersionRepository;

    public Optional<AppVersion> find(AppOs os) {
        return appVersionRepository.findByOs(os);
    }
}
