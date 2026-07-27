package com.ssoss.ssossbackend.store.application.service;

import com.ssoss.ssossbackend.store.domain.service.StoreWriter;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreWriter storeWriter;

    public void create(Long memberId) {
        storeWriter.create(memberId);
    }

    public void deleteByMemberId(Long memberId) {
        storeWriter.deleteByMemberId(memberId);
    }
}
