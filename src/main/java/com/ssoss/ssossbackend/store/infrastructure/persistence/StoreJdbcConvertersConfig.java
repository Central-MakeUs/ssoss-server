package com.ssoss.ssossbackend.store.infrastructure.persistence;

import java.util.List;

import com.ssoss.ssossbackend.persistence.JdbcConverters;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.json.JsonMapper;

@Configuration
class StoreJdbcConvertersConfig {

    @Bean
    JdbcConverters storeJdbcConverters(JsonMapper jsonMapper) {
        return new JdbcConverters(List.of(
            new BusinessDaysReadingConverter(jsonMapper),
            new BusinessDaysWritingConverter(jsonMapper),
            new SignatureMenusReadingConverter(jsonMapper),
            new SignatureMenusWritingConverter(jsonMapper),
            new StoreKeywordsReadingConverter(jsonMapper),
            new StoreKeywordsWritingConverter(jsonMapper)));
    }
}
