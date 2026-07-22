package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.time.Duration;

import com.ssoss.ssossbackend.content.domain.contract.ContentGenerator;
import com.ssoss.ssossbackend.content.domain.model.GeneratedContent;
import com.ssoss.ssossbackend.content.domain.model.GenerationMaterial;
import com.ssoss.ssossbackend.content.domain.model.GenerationResultStatus;
import com.ssoss.ssossbackend.content.domain.model.LlmCallFailedException;
import com.ssoss.ssossbackend.content.domain.model.LlmCallReply;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class GeminiContentGenerator implements ContentGenerator {

    private static final BeanOutputConverter<TitledGenerationOutput> TITLED_CONVERTER =
        new BeanOutputConverter<>(TitledGenerationOutput.class);
    private static final BeanOutputConverter<UntitledGenerationOutput> UNTITLED_CONVERTER =
        new BeanOutputConverter<>(UntitledGenerationOutput.class);

    private final GoogleGenAiChatModel chatModel;
    private final GenerationPromptComposer promptComposer;
    private final GeminiCallOutcomeClassifier outcomeClassifier;

    @Override
    public LlmCallReply generate(GenerationMaterial material) {
        BeanOutputConverter<?> converter = material.channel().hasTitle() ? TITLED_CONVERTER : UNTITLED_CONVERTER;
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
            .model(chatModel.getOptions().getModel())
            .responseMimeType(MediaType.APPLICATION_JSON_VALUE)
            .responseSchema(converter.getJsonSchema())
            .build();
        long startedAtNanos = System.nanoTime();
        ChatResponse response;
        try {
            response = chatModel.call(new Prompt(promptComposer.compose(material), options));
        } catch (RuntimeException e) {
            throw new LlmCallFailedException(outcomeClassifier.classify(e),
                Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis(), e);
        }
        long responseTimeMillis = Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis();
        Usage usage = response.getMetadata().getUsage();
        String text = null;
        try {
            text = response.getResult().getOutput().getText();
            Object output = converter.convert(text);
            if (output instanceof TitledGenerationOutput titled) {
                return new LlmCallReply(new GeneratedContent(titled.title(), titled.body(), titled.hashtags()),
                    responseTimeMillis, usage.getPromptTokens(), usage.getCompletionTokens(), text);
            }
            UntitledGenerationOutput untitled = (UntitledGenerationOutput) output;
            return new LlmCallReply(new GeneratedContent(null, untitled.body(), untitled.hashtags()),
                responseTimeMillis, usage.getPromptTokens(), usage.getCompletionTokens(), text);
        } catch (RuntimeException e) {
            throw new LlmCallFailedException(GenerationResultStatus.EMPTY_OUTPUT, responseTimeMillis,
                usage.getPromptTokens(), usage.getCompletionTokens(), text, e);
        }
    }
}
