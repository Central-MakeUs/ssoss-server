package com.ssoss.ssossbackend.content.application.command;

public record ContentRenameCommand(Long memberId, Long contentId, String name) {
}
