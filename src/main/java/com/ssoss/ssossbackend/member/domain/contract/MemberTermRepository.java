package com.ssoss.ssossbackend.member.domain.contract;

import java.util.Collection;

import com.ssoss.ssossbackend.member.domain.model.MemberTerm;

import org.springframework.data.repository.CrudRepository;

public interface MemberTermRepository extends CrudRepository<MemberTerm, Long> {

    int deleteAllByMemberIdIn(Collection<Long> memberIds);
}
