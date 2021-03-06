package dev.jshfx.jfx.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.jshfx.j.TRunnable;
import javafx.concurrent.Task;

public class TaskQueuer {

    private TaskQueue defaultTaskQueue = new TaskQueue();
    private Map<String, TaskQueue> taskQueues = new ConcurrentHashMap<>();

    public <T> Task<T> add(Task<T> task) {
        defaultTaskQueue.add(task);
        return task;
    }

    public Task<Void> add(TRunnable task) {
        Task<Void> t = QueueTask.create(() -> task.run());
        defaultTaskQueue.add(t);
        return t;
    }

    public <T> Task<T> add(QueueTask<T> task) {

        if (task.queueId() == null) {
            defaultTaskQueue.add(task);
        } else {
            taskQueues.computeIfAbsent(task.queueId(), k -> new TaskQueue()).add(task);
        }
        return task;
    }

    public Task<Void> add(String queueId, TRunnable task) {
        Task<Void> t = QueueTask.create(() -> task.run());
        taskQueues.computeIfAbsent(queueId, k -> new TaskQueue()).add(t);
        return t;
    }

    public void clear() {
        taskQueues.values().forEach(q -> q.clear());
    }
}
