package com.ssoss.ssossbackend.support;

import java.util.List;
import java.util.stream.StreamSupport;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.contract.TokenHasher;
import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;
import com.ssoss.ssossbackend.content.domain.contract.ContentChannelHistoryRepository;
import com.ssoss.ssossbackend.content.domain.contract.ContentChannelRepository;
import com.ssoss.ssossbackend.content.domain.contract.ContentRepository;
import com.ssoss.ssossbackend.content.domain.contract.GenerationRepository;
import com.ssoss.ssossbackend.content.domain.contract.GenerationResultRepository;
import com.ssoss.ssossbackend.content.domain.model.Content;
import com.ssoss.ssossbackend.content.domain.model.ContentChannel;
import com.ssoss.ssossbackend.content.domain.model.ContentChannelHistory;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.credit.domain.contract.CreditLedgerRepository;
import com.ssoss.ssossbackend.credit.domain.model.CreditLedger;
import com.ssoss.ssossbackend.credit.domain.model.CreditLedgerType;
import com.ssoss.ssossbackend.hashtag.domain.contract.HashtagBundleBookmarkRepository;
import com.ssoss.ssossbackend.hashtag.domain.model.HashtagBundleBookmark;
import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberTermRepository;
import com.ssoss.ssossbackend.member.domain.model.MemberTerm;

import org.springframework.jdbc.core.simple.JdbcClient;

import static com.ssoss.ssossbackend.member.domain.model.SocialProvider.NAVER;

public class TestDatabase {

    private final MemberRepository memberRepository;
    private final MemberTermRepository memberTermRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHasher tokenHasher;
    private final ContentRepository contentRepository;
    private final ContentChannelRepository contentChannelRepository;
    private final ContentChannelHistoryRepository contentChannelHistoryRepository;
    private final GenerationRepository generationRepository;
    private final GenerationResultRepository generationResultRepository;
    private final CreditLedgerRepository creditLedgerRepository;
    private final HashtagBundleBookmarkRepository hashtagBundleBookmarkRepository;
    private final JdbcClient jdbcClient;

    TestDatabase(MemberRepository memberRepository, MemberTermRepository memberTermRepository,
        RefreshTokenRepository refreshTokenRepository, TokenHasher tokenHasher, ContentRepository contentRepository,
        ContentChannelRepository contentChannelRepository,
        ContentChannelHistoryRepository contentChannelHistoryRepository, GenerationRepository generationRepository,
        GenerationResultRepository generationResultRepository, CreditLedgerRepository creditLedgerRepository,
        HashtagBundleBookmarkRepository hashtagBundleBookmarkRepository, JdbcClient jdbcClient) {
        this.memberRepository = memberRepository;
        this.memberTermRepository = memberTermRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenHasher = tokenHasher;
        this.contentRepository = contentRepository;
        this.contentChannelRepository = contentChannelRepository;
        this.contentChannelHistoryRepository = contentChannelHistoryRepository;
        this.generationRepository = generationRepository;
        this.generationResultRepository = generationResultRepository;
        this.creditLedgerRepository = creditLedgerRepository;
        this.hashtagBundleBookmarkRepository = hashtagBundleBookmarkRepository;
        this.jdbcClient = jdbcClient;
    }

    public Long memberIdOf(String socialId) {
        return memberRepository.findByProviderAndSocialId(NAVER, socialId).orElseThrow().getId();
    }

    public List<MemberTerm> termsOf(Long memberId) {
        return StreamSupport.stream(memberTermRepository.findAll().spliterator(), false)
            .filter(term -> term.getMemberId().equals(memberId))
            .toList();
    }

    public RefreshToken refreshTokenOf(String socialId, String rawRefreshToken) {
        return refreshTokenRepository.findAllByMemberId(memberIdOf(socialId)).stream()
            .filter(row -> row.getTokenHash().equals(tokenHasher.hash(rawRefreshToken)))
            .findFirst()
            .orElseThrow();
    }

    public List<Content> contentsOf(Long memberId) {
        return contentRepository.findAll().stream()
            .filter(content -> content.getMemberId().equals(memberId))
            .toList();
    }

    public List<ContentChannel> channelsOf(Long memberId) {
        return contentChannelRepository.findAllByMemberId(memberId);
    }

    public ContentChannel channelById(Long contentChannelId) {
        return contentChannelRepository.findById(contentChannelId).orElseThrow();
    }

    public List<ContentChannelHistory> historiesOf(Long contentChannelId) {
        return contentChannelHistoryRepository.findAll().stream()
            .filter(history -> history.getContentChannelId().equals(contentChannelId))
            .toList();
    }

    public List<Generation> generationsOf(Long memberId) {
        return generationRepository.findAllByMemberId(memberId);
    }

    public List<GenerationResult> resultsOf(Long generationId) {
        return generationResultRepository.findAllByGenerationIdOrderById(generationId);
    }

    public long generationLocksOf(Long memberId) {
        return jdbcClient.sql("SELECT COUNT(*) FROM generation_lock WHERE member_id = :memberId")
            .param("memberId", memberId)
            .query(Long.class)
            .single();
    }

    public List<CreditLedger> ledgerOf(Long memberId) {
        return creditLedgerRepository.findAll().stream()
            .filter(entry -> entry.getMemberId().equals(memberId))
            .toList();
    }

    public List<CreditLedger> deductionsOf(Long memberId) {
        return ledgerOf(memberId).stream()
            .filter(entry -> entry.getType() == CreditLedgerType.DEDUCT)
            .toList();
    }

    public List<HashtagBundleBookmark> bookmarksOf(Long memberId) {
        return hashtagBundleBookmarkRepository.findAll().stream()
            .filter(bookmark -> bookmark.getMemberId().equals(memberId))
            .toList();
    }
}
