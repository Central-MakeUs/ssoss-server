package com.ssoss.ssossbackend.content.domain.contract;

import java.util.Collection;

import com.ssoss.ssossbackend.content.domain.model.ContentChannelHistory;

import org.springframework.data.repository.ListCrudRepository;

public interface ContentChannelHistoryRepository extends ListCrudRepository<ContentChannelHistory, Long> {

    void deleteAllByContentChannelIdIn(Collection<Long> contentChannelIds);
}
