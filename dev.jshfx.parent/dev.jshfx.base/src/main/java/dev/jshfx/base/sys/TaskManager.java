package dev.jshfx.base.sys;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import dev.jshfx.j.util.concurrent.ExecutorServiceUtils;
import dev.jshfx.j.util.concurrent.PrivilegedForkJoinWorkerThreadFactory;
import javafx.concurrent.Task;

public final class TaskManager extends Manager {

	private static final TaskManager INSTANCE = new TaskManager();
	private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	private final ExecutorService executorService = Executors.newWorkStealingPool();
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	private TaskManager() {
	}
	
	@Override
	public void init() throws Exception {
		System.setProperty("java.util.concurrent.ForkJoinPool.common.threadFactory", PrivilegedForkJoinWorkerThreadFactory.class.getName());
	}

	public static TaskManager get() {
		return INSTANCE;
	}
	
	public void execute(Task<?> task) {

		executorService.execute(() -> task.run());
	}

	public void executeSequentially(Runnable task) {

		singleThreadExecutor.execute(task);
	}

	public ScheduledFuture<?> scheduleAtFixedRate​(Runnable task, long initialDelay, long period, TimeUnit unit) {
		ScheduledFuture<?> future = scheduledExecutorService.scheduleAtFixedRate(task, initialDelay, period, unit);
		return future;
	}

	@Override
	public void stop() {
	    ExecutorServiceUtils.shutdown(singleThreadExecutor);
	    ExecutorServiceUtils.shutdown(executorService);
	    ExecutorServiceUtils.shutdown(scheduledExecutorService);

		ForkJoinPool.commonPool().awaitQuiescence(60, TimeUnit.SECONDS);
	}
}
