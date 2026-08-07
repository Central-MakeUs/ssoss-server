package com.ssoss.ssossbackend.template.infrastructure.persistence;

import java.util.List;

import com.ssoss.ssossbackend.persistence.JdbcConverters;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.json.JsonMapper;

@Configuration
class TemplateJdbcConvertersConfig {

    @Bean
    JdbcConverters templateJdbcConverters(JsonMapper jsonMapper) {
        return new JdbcConverters(List.of(
            new RecommendedChannelsReadingConverter(jsonMapper),
            new RecommendedChannelsWritingConverter(jsonMapper)));
    }
}
