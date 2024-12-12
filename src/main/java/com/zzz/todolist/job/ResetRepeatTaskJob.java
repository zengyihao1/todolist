package com.zzz.todolist.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import com.zzz.todolist.service.ListInfoService;

@Component
public class ResetRepeatTaskJob extends QuartzJobBean {
    
    @Autowired
    private ListInfoService listInfoService;
    
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            listInfoService.resetRepeatTasks();
        } catch (Exception e) {
            throw new JobExecutionException("Reset repeat tasks failed", e);
        }
    }
} 