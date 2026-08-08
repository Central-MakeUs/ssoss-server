package com.ssoss.ssossbackend.template.application.command;

public record SavedTemplateEditCommand(Long memberId, Long savedTemplateId, String title, String body) {
}
