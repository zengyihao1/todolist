package com.zzz.todolist.task;

import java.util.Random;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 状态监控器，用于监控系统状态
 */
@Component
public class StatusMonitorCallable implements Callable<String> {
    
    private static final Logger logger = LoggerFactory.getLogger(StatusMonitorCallable.class);
    private volatile boolean running = true;
    private final Random random = new Random();
    
    @Override
    public String call() throws Exception {
        while (running) {
            try {
                // 70%的概率抛出异常
                if (random.nextDouble() < 0.7) {
                    throw new RuntimeException("状态监控器模拟异常");
                }
                
                logger.info("状态监控器正在运行中...");
                Thread.sleep(3000); // 每3秒打印一次
            } catch (InterruptedException e) {
                running = false;
                Thread.currentThread().interrupt();
                return "状态监控器因中断而停止";
            }
        }
        return "状态监控器已停止";
    }
    
    /**
     * 停止监控
     */
    public void stop() {
        running = false;
    }
} 