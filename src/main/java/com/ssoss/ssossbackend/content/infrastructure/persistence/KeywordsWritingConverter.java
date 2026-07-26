package com.ssoss.ssossbackend.content.infrastructure.persistence;

import com.ssoss.ssossbackend.content.domain.model.Keywords;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import tools.jackson.databind.json.JsonMapper;

@WritingConverter
@RequiredArgsConstructor
class KeywordsWritingConverter implements Converter<Keywords, String> {

    private final JsonMapper jsonMapper;

    @Override
    public String convert(Keywords source) {
        return jsonMapper.writeValueAsString(source.values());
    }
}
