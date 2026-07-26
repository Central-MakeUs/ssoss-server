package com.ssoss.ssossbackend.content.domain.contract;

import java.util.Collection;
import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.Content;

import org.springframework.data.repository.ListCrudRepository;

public interface ContentRepository extends ListCrudRepository<Content, Long> {

    List<Content> findAllByGenerationResultIdIn(Collection<Long> generationResultIds);
}
