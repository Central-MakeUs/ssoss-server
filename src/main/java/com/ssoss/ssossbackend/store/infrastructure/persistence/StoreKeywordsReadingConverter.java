package com.ssoss.ssossbackend.store.infrastructure.persistence;

import java.util.List;

import com.ssoss.ssossbackend.store.domain.model.StoreKeywords;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@ReadingConverter
@RequiredArgsConstructor
class StoreKeywordsReadingConverter implements Converter<String, StoreKeywords> {

    private final JsonMapper jsonMapper;

    @Override
    public StoreKeywords convert(String source) {
        return new StoreKeywords(jsonMapper.readValue(source, new TypeReference<List<String>>() {
        }));
    }
}
