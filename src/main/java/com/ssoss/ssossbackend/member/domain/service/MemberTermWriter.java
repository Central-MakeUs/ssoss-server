package com.ssoss.ssossbackend.member.domain.service;

import java.util.Collection;

import com.ssoss.ssossbackend.member.domain.contract.MemberTermRepository;
import com.ssoss.ssossbackend.member.domain.model.MemberTerm;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberTermWriter {

    private final MemberTermRepository memberTermRepository;

    public MemberTerm record(MemberTerm term) {
        return memberTermRepository.save(term);
    }

    public int deleteAllByMemberIds(Collection<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return 0;
        }
        return memberTermRepository.deleteAllByMemberIdIn(memberIds);
    }
}
