package ca.uqac.inf135.group3.tp2.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskPool {
    //Limit to 50 concurrent tasks
    private static int CONCURRENT_TASKS = 50;
    private static ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_TASKS);

    public static void submitTask(Runnable task) {
        executor.execute(task);
    }
}
