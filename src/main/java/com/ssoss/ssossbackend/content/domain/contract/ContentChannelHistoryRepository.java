package com.ssoss.ssossbackend.content.domain.contract;

import com.ssoss.ssossbackend.content.domain.model.ContentChannelHistory;

import org.springframework.data.repository.ListCrudRepository;

public interface ContentChannelHistoryRepository extends ListCrudRepository<ContentChannelHistory, Long> {
}
