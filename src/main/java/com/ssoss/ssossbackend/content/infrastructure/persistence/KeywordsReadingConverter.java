package com.ssoss.ssossbackend.content.infrastructure.persistence;

import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.Keywords;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@ReadingConverter
@RequiredArgsConstructor
class KeywordsReadingConverter implements Converter<String, Keywords> {

    private final JsonMapper jsonMapper;

    @Override
    public Keywords convert(String source) {
        return new Keywords(jsonMapper.readValue(source, new TypeReference<List<String>>() {
        }));
    }
}
