package com.ssoss.ssossbackend.credit.domain.service;

import java.util.List;
import java.util.Optional;

import com.ssoss.ssossbackend.credit.domain.contract.CreditRepository;
import com.ssoss.ssossbackend.credit.domain.model.Credit;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditFinder {

    private final CreditRepository creditRepository;

    public Optional<Credit> find(Long memberId) {
        return creditRepository.findByMemberId(memberId);
    }

    public List<Long> findAllMemberIds() {
        return creditRepository.findAll().stream()
            .map(Credit::getMemberId)
            .toList();
    }
}
