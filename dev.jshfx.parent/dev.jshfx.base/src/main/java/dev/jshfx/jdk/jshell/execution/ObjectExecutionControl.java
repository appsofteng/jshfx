package dev.jshfx.jdk.jshell.execution;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import dev.jshfx.j.util.concurrent.ExecutorServiceUtils;
import jdk.jshell.execution.DirectExecutionControl;

public class ObjectExecutionControl extends DirectExecutionControl {

    private ExecutorService executorService;
    private ReentrantLock lock = new ReentrantLock();
    private List<Object> results = Collections.synchronizedList(new ArrayList<>());

    public ObjectExecutionControl() {
        executorService = getExecutorService();
    }

    public List<Object> getResults() {
        return results;
    }

    @Override
    protected String invoke(Method doitMethod) throws Exception {
        var future = executorService.submit(() -> doitMethod.invoke(null, new Object[0]));

        Object result = future.get();
        results.add(result);

        return valueString(result);
    }

    public void stop() {
        lock.lock();
        try {
            ExecutorServiceUtils.shutdown(executorService, 5);
            executorService = getExecutorService();
        } finally {
            lock.unlock();
        }
    }

    private ExecutorService getExecutorService() {
        return Executors.newSingleThreadExecutor();
    }
}
