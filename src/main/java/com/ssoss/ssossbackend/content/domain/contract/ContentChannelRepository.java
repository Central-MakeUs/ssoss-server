package com.ssoss.ssossbackend.content.domain.contract;

import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.ContentChannel;

import org.springframework.data.repository.ListCrudRepository;

public interface ContentChannelRepository extends ListCrudRepository<ContentChannel, Long> {

    List<ContentChannel> findAllByContentId(Long contentId);

    List<ContentChannel> findAllByContentIdAndDeletedAtIsNull(Long contentId);
}
