package utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPoolManager {

    private static final ExecutorService executor = Executors.newFixedThreadPool(24);

    public static void submit(Runnable task) {
        executor.submit(task);
    }

    public static <T> Future<T> submitTask(java.util.concurrent.Callable<T> task) {
        return executor.submit(task);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}