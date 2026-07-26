package com.ssoss.ssossbackend.content.infrastructure.persistence;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.repository.config.JdbcConfiguration;

import tools.jackson.databind.json.JsonMapper;

@Configuration
class ContentJdbcConversionsConfig {

    @Bean
    JdbcCustomConversions jdbcCustomConversions(JdbcDialect dialect, JsonMapper jsonMapper) {
        return JdbcConfiguration.createCustomConversions(dialect, List.of(
            new HashtagsReadingConverter(jsonMapper),
            new HashtagsWritingConverter(jsonMapper),
            new KeywordsReadingConverter(jsonMapper),
            new KeywordsWritingConverter(jsonMapper)));
    }
}
