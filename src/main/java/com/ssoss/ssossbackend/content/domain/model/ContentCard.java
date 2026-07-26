package com.ssoss.ssossbackend.content.domain.model;

import java.util.List;

public record ContentCard(Content content, List<Channel> channels, ContentChannel representative) {
}
