package com.ssoss.ssossbackend.store.domain.contract;

import java.util.Optional;

import com.ssoss.ssossbackend.store.domain.model.Store;

import org.springframework.data.repository.ListCrudRepository;

public interface StoreRepository extends ListCrudRepository<Store, Long> {

    Optional<Store> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
