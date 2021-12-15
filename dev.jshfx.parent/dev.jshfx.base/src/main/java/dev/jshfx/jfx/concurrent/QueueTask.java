package dev.jshfx.jfx.concurrent;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import dev.jshfx.j.TRunnable;
import javafx.concurrent.Task;

public abstract class QueueTask<V> extends Task<V> {

    private String queueId;    
    
    public String queueId() {
        return queueId;
    }
    
    public QueueTask<V> queueId(String value) {
        queueId = value;
        
        return this;
    }
    
    public static QueueTask<Void> create(TRunnable task) {
        QueueTask<Void> t = new QueueTask<>() {

            @Override
            protected Void call() throws Exception {
                task.run();
                return null;
            }
        };

        t.setOnFailed(e -> {
            throw new RuntimeException(e.getSource().getException());
        });

        return t;
    }

    public static <T> QueueTask<T> create(Callable<T> task) {
        QueueTask<T> t = new QueueTask<>() {

            @Override
            protected T call() throws Exception {
                return task.call();
            }
        };

        t.setOnFailed(e -> {
            throw new RuntimeException(e.getSource().getException());
        });

        return t;
    }

    public QueueTask<V> onFinished(Consumer<QueueTask<V>> value) {

        setOnSucceeded(e -> value.accept(this));
        setOnCancelled(e -> value.accept(this));
        setOnFailed(e -> value.accept(this));

        return this;
    }

    public QueueTask<V> onSucceeded(Consumer<V> value) {

        setOnSucceeded(e -> value.accept(getValue()));
        return this;
    }
    
    public QueueTask<V> onFailed(Consumer<V> value) {

        setOnFailed(e -> value.accept(getValue()));
        return this;
    }
}
