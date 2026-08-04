package com.ssoss.ssossbackend.hashtag.application.service;

import com.ssoss.ssossbackend.hashtag.application.result.HashtagBundleListResult;
import com.ssoss.ssossbackend.hashtag.domain.service.HashtagBundleFinder;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HashtagBundleService {

    private final HashtagBundleFinder hashtagBundleFinder;

    public HashtagBundleListResult list(int page, int size) {
        return HashtagBundleListResult.from(hashtagBundleFinder.list(page, size));
    }
}
