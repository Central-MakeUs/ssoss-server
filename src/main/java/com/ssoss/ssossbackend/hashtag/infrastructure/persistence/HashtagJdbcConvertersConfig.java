package com.ssoss.ssossbackend.hashtag.infrastructure.persistence;

import java.util.List;

import com.ssoss.ssossbackend.persistence.JdbcConverters;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.json.JsonMapper;

@Configuration
class HashtagJdbcConvertersConfig {

    @Bean
    JdbcConverters hashtagJdbcConverters(JsonMapper jsonMapper) {
        return new JdbcConverters(List.of(
            new HashtagsReadingConverter(jsonMapper),
            new HashtagsWritingConverter(jsonMapper)));
    }
}
