package com.ssoss.ssossbackend.member.application.event;

import java.util.List;

public record MembersDeletedEvent(List<Long> memberIds) {
}
