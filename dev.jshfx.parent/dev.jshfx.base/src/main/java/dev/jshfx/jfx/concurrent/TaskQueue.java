package dev.jshfx.jfx.concurrent;

import java.util.ArrayDeque;
import java.util.Queue;

import dev.jshfx.base.sys.TaskManager;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;

public class TaskQueue {

    private Queue<Task<?>> queue = new ArrayDeque<>();

    public synchronized void add(Task<?> task) {
        queue.add(task);

        task.stateProperty().addListener((v, o, n) -> {
            if (n == State.SUCCEEDED || n == State.CANCELLED || n == State.FAILED) {
                remove(task);
            }
        });

        if (queue.size() == 1) {

            TaskManager.get().execute(task);
        }
    }

    public synchronized void remove(Task<?> task) {
        queue.remove(task);

        if (queue.size() > 0) {
        	 TaskManager.get().execute(queue.peek());
        }
    }
}
