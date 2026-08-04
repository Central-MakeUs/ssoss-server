package com.ssoss.ssossbackend.hashtag.infrastructure.persistence;

import java.util.List;

import com.ssoss.ssossbackend.hashtag.domain.model.Hashtags;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@ReadingConverter
@RequiredArgsConstructor
class HashtagsReadingConverter implements Converter<String, Hashtags> {

    private final JsonMapper jsonMapper;

    @Override
    public Hashtags convert(String source) {
        return new Hashtags(jsonMapper.readValue(source, new TypeReference<List<String>>() {
        }));
    }
}
