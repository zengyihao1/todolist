package com.zzz.todolist.config;

import java.util.TimeZone;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zzz.todolist.job.ResetRepeatTaskJob;

@Configuration
public class QuartzConfig {
    
    @Bean
    public JobDetail resetRepeatTaskJobDetail() {
        return JobBuilder.newJob(ResetRepeatTaskJob.class)
                .withIdentity("resetRepeatTaskJob")
                .storeDurably()
                .build();
    }
    
    @Bean
    public Trigger resetRepeatTaskTrigger() {
        // 创建cron表达式，东八区每天凌晨3点执行
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                .cronSchedule("0 0 3 * * ?")
                .inTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        
        return TriggerBuilder.newTrigger()
                .forJob(resetRepeatTaskJobDetail())
                .withIdentity("resetRepeatTaskTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }
} 