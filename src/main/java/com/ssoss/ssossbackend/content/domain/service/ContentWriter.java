package com.ssoss.ssossbackend.content.domain.service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ssoss.ssossbackend.content.domain.contract.ContentRepository;
import com.ssoss.ssossbackend.content.domain.model.Content;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentWriter {

    private final ContentRepository contentRepository;

    @Transactional
    public List<Content> save(Long memberId, List<GenerationResult> results) {
        List<Content> saved = contentRepository.findAllByGenerationResultIdIn(
            results.stream().map(GenerationResult::getId).toList());
        Set<Long> savedResultIds = saved.stream()
            .map(Content::getGenerationResultId)
            .collect(Collectors.toSet());
        List<Content> added = contentRepository.saveAll(results.stream()
            .filter(result -> !savedResultIds.contains(result.getId()))
            .map(result -> Content.copyOf(memberId, result))
            .toList());
        return Stream.concat(saved.stream(), added.stream())
            .sorted(Comparator.comparing(Content::getChannel))
            .toList();
    }
}
