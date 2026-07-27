package com.ssoss.ssossbackend.content.infrastructure.persistence;

import java.util.List;

import com.ssoss.ssossbackend.persistence.JdbcConverters;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.json.JsonMapper;

@Configuration
class ContentJdbcConvertersConfig {

    @Bean
    JdbcConverters contentJdbcConverters(JsonMapper jsonMapper) {
        return new JdbcConverters(List.of(
            new HashtagsReadingConverter(jsonMapper),
            new HashtagsWritingConverter(jsonMapper),
            new KeywordsReadingConverter(jsonMapper),
            new KeywordsWritingConverter(jsonMapper)));
    }
}
