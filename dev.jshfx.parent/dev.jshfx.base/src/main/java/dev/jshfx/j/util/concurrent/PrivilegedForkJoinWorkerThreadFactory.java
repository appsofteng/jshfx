package dev.jshfx.j.util.concurrent;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class PrivilegedForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

    @Override
    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        return new PrivilegedForkJoinWorkerThread(pool);
    }
}
