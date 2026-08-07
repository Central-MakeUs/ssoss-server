package com.ssoss.ssossbackend.template.infrastructure.persistence;

import java.util.List;

import com.ssoss.ssossbackend.template.domain.model.Channel;
import com.ssoss.ssossbackend.template.domain.model.RecommendedChannels;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@ReadingConverter
@RequiredArgsConstructor
class RecommendedChannelsReadingConverter implements Converter<String, RecommendedChannels> {

    private final JsonMapper jsonMapper;

    @Override
    public RecommendedChannels convert(String source) {
        return new RecommendedChannels(jsonMapper.readValue(source, new TypeReference<List<Channel>>() {
        }));
    }
}
