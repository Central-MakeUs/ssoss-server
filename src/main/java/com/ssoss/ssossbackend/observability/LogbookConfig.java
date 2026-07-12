package com.ssoss.ssossbackend.observability;

import java.util.Locale;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.HeaderFilter;
import org.zalando.logbook.core.HeaderFilters;
import org.zalando.logbook.json.JsonBodyFilters;

@Configuration
class LogbookConfig {

    private static final Set<String> LOGGED_HEADERS = Set.of("content-type", "authorization");
    private static final Set<String> MASKED_BODY_PROPERTIES = Set.of("accessToken", "refreshToken");

    @Bean
    HeaderFilter loggedHeaderFilter() {
        HeaderFilter allowlist = HeaderFilters.removeHeaders(name -> !LOGGED_HEADERS.contains(name.toLowerCase(Locale.ROOT)));
        return HeaderFilter.merge(allowlist, HeaderFilters.authorization());
    }

    @Bean
    BodyFilter credentialMaskingBodyFilter() {
        return JsonBodyFilters.replaceJsonStringProperty(MASKED_BODY_PROPERTIES, "***");
    }
}
