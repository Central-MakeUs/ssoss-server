package com.ssoss.ssossbackend.member.domain.model;

import java.time.Instant;

import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("member_term")
public class MemberTerm {

    @Id
    private Long id;
    private Long memberId;
    private boolean ageOver14Agreed;
    private boolean serviceTermsAgreed;
    private boolean privacyPolicyAgreed;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    MemberTerm(Long id, Long memberId, boolean ageOver14Agreed, boolean serviceTermsAgreed,
        boolean privacyPolicyAgreed) {
        this.id = id;
        this.memberId = memberId;
        this.ageOver14Agreed = ageOver14Agreed;
        this.serviceTermsAgreed = serviceTermsAgreed;
        this.privacyPolicyAgreed = privacyPolicyAgreed;
    }

    public static MemberTerm record(Long memberId, boolean ageOver14Agreed, boolean serviceTermsAgreed,
        boolean privacyPolicyAgreed) {
        if (!ageOver14Agreed || !serviceTermsAgreed || !privacyPolicyAgreed) {
            throw new BusinessException(TermErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
        return new MemberTerm(null, memberId, ageOver14Agreed, serviceTermsAgreed, privacyPolicyAgreed);
    }
}
