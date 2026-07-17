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
    private boolean serviceTermsAgreed;
    private boolean privacyPolicyAgreed;
    private boolean marketingAgreed;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    MemberTerm(Long id, Long memberId, boolean serviceTermsAgreed, boolean privacyPolicyAgreed,
        boolean marketingAgreed) {
        this.id = id;
        this.memberId = memberId;
        this.serviceTermsAgreed = serviceTermsAgreed;
        this.privacyPolicyAgreed = privacyPolicyAgreed;
        this.marketingAgreed = marketingAgreed;
    }

    public static MemberTerm record(Long memberId, boolean serviceTermsAgreed, boolean privacyPolicyAgreed,
        boolean marketingAgreed) {
        if (!serviceTermsAgreed || !privacyPolicyAgreed) {
            throw new BusinessException(TermErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
        return new MemberTerm(null, memberId, serviceTermsAgreed, privacyPolicyAgreed, marketingAgreed);
    }
}
