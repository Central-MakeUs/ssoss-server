package com.ssoss.ssossbackend.support;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.core.task.TaskExecutor;

public class TestTaskExecutor implements TaskExecutor {

    private final Queue<Runnable> heldTasks = new ConcurrentLinkedQueue<>();
    private volatile boolean holding = false;

    @Override
    public void execute(Runnable task) {
        if (holding) {
            heldTasks.add(task);
            return;
        }
        task.run();
    }

    public void hold() {
        holding = true;
    }

    public void release() {
        holding = false;
        Runnable task;
        while ((task = heldTasks.poll()) != null) {
            task.run();
        }
    }

    public void reset() {
        holding = false;
        heldTasks.clear();
    }
}
