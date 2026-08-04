package com.ssoss.ssossbackend.hashtag.domain.service;

import com.ssoss.ssossbackend.hashtag.domain.contract.HashtagBundleRepository;
import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundle;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HashtagBundleFinder {

    private static final Sort LATEST_FIRST = Sort.by(Sort.Direction.DESC, "id");

    private final HashtagBundleRepository hashtagBundleRepository;

    public Page<HashtagBundle> list(int page, int size) {
        return hashtagBundleRepository.findAll(PageRequest.of(page, size, LATEST_FIRST));
    }
}
