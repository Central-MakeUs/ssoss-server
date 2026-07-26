package com.ssoss.ssossbackend.content.domain.model;

import java.util.List;

public record ContentWithChannels(Content content, List<ContentChannel> channels) {
}
