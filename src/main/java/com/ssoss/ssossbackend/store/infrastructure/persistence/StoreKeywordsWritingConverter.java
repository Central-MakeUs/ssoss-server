package com.ssoss.ssossbackend.store.infrastructure.persistence;

import com.ssoss.ssossbackend.store.domain.model.StoreKeywords;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import tools.jackson.databind.json.JsonMapper;

@WritingConverter
@RequiredArgsConstructor
class StoreKeywordsWritingConverter implements Converter<StoreKeywords, String> {

    private final JsonMapper jsonMapper;

    @Override
    public String convert(StoreKeywords source) {
        return jsonMapper.writeValueAsString(source.values());
    }
}
