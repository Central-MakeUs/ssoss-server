package com.ssoss.ssossbackend.store.infrastructure.persistence;

import java.util.List;

import com.ssoss.ssossbackend.store.domain.model.SignatureMenus;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@ReadingConverter
@RequiredArgsConstructor
class SignatureMenusReadingConverter implements Converter<String, SignatureMenus> {

    private final JsonMapper jsonMapper;

    @Override
    public SignatureMenus convert(String source) {
        return new SignatureMenus(jsonMapper.readValue(source, new TypeReference<List<String>>() {
        }));
    }
}
