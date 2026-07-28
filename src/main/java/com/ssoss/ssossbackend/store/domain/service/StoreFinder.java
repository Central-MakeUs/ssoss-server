package com.ssoss.ssossbackend.store.domain.service;

import java.util.Optional;

import com.ssoss.ssossbackend.store.domain.contract.StoreRepository;
import com.ssoss.ssossbackend.store.domain.model.Store;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreFinder {

    private final StoreRepository storeRepository;

    public Optional<Store> find(Long memberId) {
        return storeRepository.findByMemberId(memberId);
    }
}
