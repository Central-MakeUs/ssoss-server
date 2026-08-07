package com.ssoss.ssossbackend.template.application.command;

public record SavedTemplateSaveCommand(Long memberId, Long templateId, String body) {
}
