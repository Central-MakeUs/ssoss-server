package com.ssoss.ssossbackend.template.infrastructure.persistence;

import com.ssoss.ssossbackend.template.domain.model.RecommendedChannels;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import tools.jackson.databind.json.JsonMapper;

@WritingConverter
@RequiredArgsConstructor
class RecommendedChannelsWritingConverter implements Converter<RecommendedChannels, String> {

    private final JsonMapper jsonMapper;

    @Override
    public String convert(RecommendedChannels source) {
        return jsonMapper.writeValueAsString(source.values());
    }
}
