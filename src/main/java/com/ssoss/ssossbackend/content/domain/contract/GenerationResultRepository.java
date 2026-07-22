package com.ssoss.ssossbackend.content.domain.contract;

import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.GenerationResult;

import org.springframework.data.repository.ListCrudRepository;

public interface GenerationResultRepository extends ListCrudRepository<GenerationResult, Long> {

    List<GenerationResult> findAllByGenerationIdOrderById(Long generationId);
}
