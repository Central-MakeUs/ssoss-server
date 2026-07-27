package com.ssoss.ssossbackend.store.infrastructure.persistence;

import com.ssoss.ssossbackend.store.domain.model.BusinessDays;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import tools.jackson.databind.json.JsonMapper;

@WritingConverter
@RequiredArgsConstructor
class BusinessDaysWritingConverter implements Converter<BusinessDays, String> {

    private final JsonMapper jsonMapper;

    @Override
    public String convert(BusinessDays source) {
        return jsonMapper.writeValueAsString(source.values());
    }
}
