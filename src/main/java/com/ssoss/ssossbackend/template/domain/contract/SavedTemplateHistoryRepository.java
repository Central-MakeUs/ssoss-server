package com.ssoss.ssossbackend.template.domain.contract;

import java.util.Collection;

import com.ssoss.ssossbackend.template.domain.model.SavedTemplateHistory;

import org.springframework.data.repository.ListCrudRepository;

public interface SavedTemplateHistoryRepository extends ListCrudRepository<SavedTemplateHistory, Long> {

    void deleteAllBySavedTemplateIdIn(Collection<Long> savedTemplateIds);
}
