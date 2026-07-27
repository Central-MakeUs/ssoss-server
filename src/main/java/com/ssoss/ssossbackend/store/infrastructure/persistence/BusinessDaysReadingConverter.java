package com.ssoss.ssossbackend.store.infrastructure.persistence;

import java.time.DayOfWeek;
import java.util.List;

import com.ssoss.ssossbackend.store.domain.model.BusinessDays;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@ReadingConverter
@RequiredArgsConstructor
class BusinessDaysReadingConverter implements Converter<String, BusinessDays> {

    private final JsonMapper jsonMapper;

    @Override
    public BusinessDays convert(String source) {
        return new BusinessDays(jsonMapper.readValue(source, new TypeReference<List<DayOfWeek>>() {
        }));
    }
}
