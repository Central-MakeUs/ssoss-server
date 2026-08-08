package com.ssoss.ssossbackend.template.application.result;

import java.util.List;

import com.ssoss.ssossbackend.template.domain.model.Channel;
import com.ssoss.ssossbackend.template.domain.model.Template;

public record TemplateDetailResult(Long id, String category, String title, String description, String body,
                                   String exampleBody, List<String> recommendedChannels, boolean bookmarked) {

    public static TemplateDetailResult from(Template template, boolean bookmarked) {
        return new TemplateDetailResult(template.getId(), template.getCategory().name(), template.getTitle(),
            template.getDescription(), template.getBody(), template.getExampleBody(),
            template.recommendedChannelList().stream().map(Channel::name).toList(), bookmarked);
    }
}
