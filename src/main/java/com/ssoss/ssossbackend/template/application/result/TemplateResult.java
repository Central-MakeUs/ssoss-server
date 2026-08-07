package com.ssoss.ssossbackend.template.application.result;

import java.util.List;

import com.ssoss.ssossbackend.template.domain.model.Channel;
import com.ssoss.ssossbackend.template.domain.model.Template;

public record TemplateResult(Long id, String category, String title, String description,
                             List<String> recommendedChannels, boolean bookmarked) {

    public static TemplateResult from(Template template) {
        return new TemplateResult(template.getId(), template.getCategory().name(), template.getTitle(),
            template.getDescription(), template.recommendedChannelList().stream().map(Channel::name).toList(),
            false);
    }
}
