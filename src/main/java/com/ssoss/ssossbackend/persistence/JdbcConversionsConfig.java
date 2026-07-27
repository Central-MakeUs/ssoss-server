package com.ssoss.ssossbackend.persistence;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.repository.config.JdbcConfiguration;

@Configuration
class JdbcConversionsConfig {

    @Bean
    JdbcCustomConversions jdbcCustomConversions(JdbcDialect dialect, List<JdbcConverters> converters) {
        return JdbcConfiguration.createCustomConversions(dialect, converters.stream()
            .flatMap(moduleConverters -> moduleConverters.values().stream())
            .toList());
    }
}
