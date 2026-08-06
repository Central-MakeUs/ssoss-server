package com.ssoss.ssossbackend.content.domain.contract;

import java.util.Collection;
import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.ContentChannel;
import com.ssoss.ssossbackend.content.domain.model.ContentChannelView;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

public interface ContentChannelRepository extends ListCrudRepository<ContentChannel, Long> {

    List<ContentChannel> findAllByContentId(Long contentId);

    List<ContentChannel> findAllByContentIdAndDeletedAtIsNull(Long contentId);

    Page<ContentChannelView> findViewsByMemberIdAndChannelAndDeletedAtIsNull(
        Long memberId, Channel channel, Pageable pageable);

    List<ContentChannelView> findViewsByContentIdInAndDeletedAtIsNull(Collection<Long> contentIds);

    List<ContentChannel> findAllByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);
}
