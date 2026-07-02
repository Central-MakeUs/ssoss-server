package com.ssoss.ssossbackend.observability;

import java.util.Locale;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.HeaderFilter;
import org.zalando.logbook.core.HeaderFilters;

@Configuration
public class LogbookConfig {

    private static final Set<String> LOGGED_HEADERS = Set.of("content-type", "authorization");

    @Bean
    HeaderFilter loggedHeaderFilter() {
        HeaderFilter allowlist = HeaderFilters.removeHeaders(name -> !LOGGED_HEADERS.contains(name.toLowerCase(Locale.ROOT)));
        return HeaderFilter.merge(allowlist, HeaderFilters.authorization());
    }
}

