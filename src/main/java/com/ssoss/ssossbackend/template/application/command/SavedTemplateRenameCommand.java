package com.ssoss.ssossbackend.template.application.command;

public record SavedTemplateRenameCommand(Long memberId, Long savedTemplateId, String title) {
}
