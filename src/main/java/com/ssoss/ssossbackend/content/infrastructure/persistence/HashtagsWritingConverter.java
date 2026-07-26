package com.ssoss.ssossbackend.content.infrastructure.persistence;

import com.ssoss.ssossbackend.content.domain.model.Hashtags;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import tools.jackson.databind.json.JsonMapper;

@WritingConverter
@RequiredArgsConstructor
class HashtagsWritingConverter implements Converter<Hashtags, String> {

    private final JsonMapper jsonMapper;

    @Override
    public String convert(Hashtags source) {
        return jsonMapper.writeValueAsString(source.values());
    }
}
