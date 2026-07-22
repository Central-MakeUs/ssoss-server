package com.ssoss.ssossbackend.content.domain.contract;

import com.ssoss.ssossbackend.content.domain.model.GenerationMaterial;
import com.ssoss.ssossbackend.content.domain.model.LlmCallReply;

public interface ContentGenerator {

    LlmCallReply generate(GenerationMaterial material);
}
