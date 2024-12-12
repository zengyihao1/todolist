package com.zzz.todolist.listener;


import com.zzz.todolist.task.StatusMonitorCallable;
import com.zzz.todolist.task.TaskMonitorCallable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.sql.Timestamp;
import java.util.concurrent.*;

@Component
public class commonListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private StatusMonitorCallable statusMonitorCallable;

    @Autowired
    private TaskMonitorCallable taskMonitorCallable;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2, 2, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = Executors.defaultThreadFactory().newThread(r);
//                    thread.setDaemon(true); // 设置为守护线程
                    return thread;
                }
            }
    ); // 创建线程池

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
//            thread.setDaemon(true); // 设置为守护线程
            return thread;
        }
    }); // 创建调度器

    private static final int MAX_RETRIES = 3; // 最大重试次数
    private static final long INITIAL_RETRY_DELAY = 1; // 初始重试延迟时间（以分钟为单位）

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        // 应用启动后，提交两个任务
        submitTaskWithRetry(statusMonitorCallable, 0);
        submitTaskWithRetry(taskMonitorCallable, 0);
    }


    // 提交任务并重试
    private void submitTaskWithRetry(Object task, int attempt) {
        if (task instanceof Runnable) {
            submitRunnableWithRetry((Runnable) task, attempt);
        } else if (task instanceof Callable) {
            submitCallableWithRetry((Callable<?>) task, attempt);
        } else {
            throw new IllegalArgumentException("Unsupported task type: " + task.getClass().getName());
        }
    }

    // 提交 Runnable 任务并重试
    private void submitRunnableWithRetry(Runnable task, int attempt) {
        Future<?> future = executor.submit(() -> {
            try {
                task.run(); // 执行任务
                System.out.println("任务 " + task.getClass().getName() + " 执行成功");
            } catch (Exception e) {
                handleTaskException(task, attempt, e);
            }
        });
        listenAndRetry(task, future, attempt);
    }

    // 提交 Callable 任务并重试
    private <T> void submitCallableWithRetry(Callable<T> task, int attempt) {
        Future<T> future = executor.submit(() -> {
            try {
                T result = task.call(); // 执行任务
                System.out.println("任务 " + task.getClass().getName() + " 执行成功，结果: " + result);

            } catch (Exception e) {
                handleTaskException(task, attempt, e);
            }
            return null; // 返回 null，因为 Callable 的返回值不需要处理
        });
        listenAndRetry(task, future, attempt);
    }

    // 监听任务状态并进行重试
    private void listenAndRetry(Object task, Future<?> future, int attempt) {
        scheduler.scheduleAtFixedRate(() -> {
            if (!future.isDone()) {
                return; // 任务未完成，继续监听
            }

            try {
                future.get(); // 获取任务的执行结果，如果有异常会在这里抛出
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("线程在等待执行结果时被中断");
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                handleTaskException(task, attempt, cause);
            }

        }, 0, 1, TimeUnit.SECONDS); // 每秒检查一次任务状态
    }

    // 处理任务异常并重试
    private void handleTaskException(Object task, int attempt, Throwable cause) {

        System.err.println("任务 " + task.getClass().getName() + " 执行异常: " + cause.getMessage());

        if (attempt < MAX_RETRIES) {
//            long delay = INITIAL_RETRY_DELAY * (1 << attempt); // 指数退避策略
            long delay = INITIAL_RETRY_DELAY; // 指数退避策略
            System.out.println("任务 " + task.getClass().getName() + " 将在 " + delay + " 分钟后重试第" + attempt+1 + "次");
            scheduleRetry(task, attempt + 1, delay);

            int i = attempt + 1;

        } else {
            System.err.println("任务 " + task.getClass().getName() + " 达到最大重试限制: " + MAX_RETRIES);
            // 触发报警逻辑，例如发送邮件或短信，现在只是记录表
            shutdownExecutorIfNeeded();
        }


    }

    // 调度任务重试
    private void scheduleRetry(Object task, int attempt, long delay) {
        scheduler.schedule(() -> {
            submitTaskWithRetry(task, attempt); // 重新提交任务
        }, delay, TimeUnit.MINUTES);
    }

    // 在应用关闭时确保关闭线程池和调度器
    @PreDestroy
    public void cleanUp() {
        executor.shutdown();
        scheduler.shutdown();
    }

    // 检查并关闭线程池
    private void shutdownExecutorIfNeeded() {
        // 检查是否有活动的任务，如果没有则关闭线程池
        if (executor.getActiveCount() == 0 && executor.getQueue().isEmpty()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("所有任务已完成，线程池已关闭");
        }
    }

}

