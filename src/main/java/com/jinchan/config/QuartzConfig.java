package com.jinchan.config;

import com.jinchan.job.DisbandExpiredTeam;
import com.jinchan.job.UserRecommendationCache;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Value("${FitBuddy.job}")
    private String jobTriggerTime;

    @Bean
    public JobDetail disbandExpireTeamJobDetail() {
        return JobBuilder.newJob(DisbandExpiredTeam.class).storeDurably().build();
    }

    @Bean
    public Trigger disbandExpireTeamTrigger() {
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(jobTriggerTime);
        return TriggerBuilder.newTrigger().forJob(disbandExpireTeamJobDetail()).withSchedule(cronScheduleBuilder).build();
    }

    @Bean
    public JobDetail userRecommendationCacheJobDetail() {
        return JobBuilder.newJob(UserRecommendationCache.class).storeDurably().build();
    }

    @Bean
    public Trigger userRecommendationCacheTrigger() {
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(jobTriggerTime);
        return TriggerBuilder.newTrigger().forJob(userRecommendationCacheJobDetail()).withSchedule(cronScheduleBuilder).build();
    }
}
