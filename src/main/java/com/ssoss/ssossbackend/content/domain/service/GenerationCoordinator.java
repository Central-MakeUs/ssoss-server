package com.ssoss.ssossbackend.content.domain.service;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.ssoss.ssossbackend.content.domain.model.Generation;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenerationCoordinator {

    private final ChannelGenerationRunner channelGenerationRunner;
    private final GenerationWriter generationWriter;
    private final Clock clock;

    @Async
    public void run(Generation generation) {
        List<Callable<Void>> channelTasks = generation.channelList().stream()
            .<Callable<Void>>map(channel -> () -> {
                channelGenerationRunner.run(generation, channel);
                return null;
            })
            .toList();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.invokeAll(channelTasks, generation.deadlineBudget(clock.instant()).toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        generationWriter.finish(generation);
    }
}
