package dev.jshfx.j.util.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class ExecutorServiceUtils {

    private static final long DEFAULT_TIMEOUT = 60;
    
    private ExecutorServiceUtils() {
    }
    
    public static void shutdown(ExecutorService executorService) {
        shutdown(executorService, DEFAULT_TIMEOUT);
    }
    
    public static void shutdown(ExecutorService executorService, long timeout) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
