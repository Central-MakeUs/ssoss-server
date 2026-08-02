package com.ssoss.ssossbackend.content.domain.service;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.model.StoreMaterial;

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
    public void run(Generation generation, StoreMaterial store) {
        List<Channel> channels = generation.channelList();
        AtomicInteger succeededChannels = new AtomicInteger();
        List<Callable<Void>> channelTasks = channels.stream()
            .<Callable<Void>>map(channel -> () -> {
                if (channelGenerationRunner.run(generation, channel, store)) {
                    succeededChannels.incrementAndGet();
                }
                return null;
            })
            .toList();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.invokeAll(channelTasks, generation.deadlineBudget(clock.instant()).toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        generationWriter.finish(generation, succeededChannels.get() == channels.size());
    }
}
