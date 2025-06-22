package com.heuk.base.service.impl;

import com.heuk.base.dto.*;
import com.heuk.base.exception.BaseException;
import com.heuk.base.service.QuartzService;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class QuartzServiceImpl implements QuartzService {
    private final Scheduler scheduler;
    private final SchedulerFactoryBean schedulerFactoryBean;

    public QuartzServiceImpl(
            Scheduler scheduler,
            SchedulerFactoryBean schedulerFactoryBean
    ) {
        this.scheduler = scheduler;
        this.schedulerFactoryBean = schedulerFactoryBean;
    }

    @Override
    public void addJob(QuartzCreateParam param) throws SchedulerException {
        String clazzName = param.getJobClazz();
        String jobName = param.getJobName();
        String jobGroup = param.getJobGroup();
        String cron = param.getCron();
        String description = param.getDescription();

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        checkJobExist(jobKey);

        Class<? extends Job> jobClass = null;
        try {
            jobClass = (Class<? extends Job>) Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            throw new BaseException("找不到任务类：" + clazzName);
        }
        JobDataMap jobDataMap = new JobDataMap();
        if (param.getJobDataMap() != null) {
            param.getJobDataMap().forEach(jobDataMap::put);
        }

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, jobGroup)
                .usingJobData(jobDataMap)
                .build();

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
        String triggerId = jobKey.getGroup() + jobKey.getName();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(scheduleBuilder)
                .withIdentity(triggerId)
                .withDescription(description)
                .build();
        scheduler.scheduleJob(jobDetail, trigger);

        if (!scheduler.isShutdown()) {
            scheduler.start();
        }
    }

    @Override
    public void updateJob(QuartzUpdateParam param) throws SchedulerException {
        String jobName = param.getJobName();
        String jobGroup = param.getJobGroup();
        String cron = param.getCron();

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        String triggerId = jobKey.getGroup() + jobKey.getName();

        checkJobExist(jobKey);

        TriggerKey triggerKey = TriggerKey.triggerKey(triggerId);
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
        TriggerBuilder<?> triggerBuilder = TriggerBuilder.newTrigger()
                .withSchedule(scheduleBuilder)
                .withIdentity(triggerId);

        Trigger trigger = triggerBuilder.build();
        scheduler.rescheduleJob(triggerKey, trigger);
    }

    @Override
    public void pauseJob(QuartzDetailParam param) throws SchedulerException {
        String jobName = param.getJobName();
        String jobGroup = param.getJobGroup();

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        checkJobExist(jobKey);

        scheduler.pauseJob(jobKey);
    }

    @Override
    public void resumeJob(QuartzDetailParam param) throws SchedulerException {
        String jobName = param.getJobName();
        String jobGroup = param.getJobGroup();

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        checkJobExist(jobKey);

        scheduler.resumeJob(jobKey);
    }

    @Override
    public void deleteJob(QuartzDetailParam param) throws SchedulerException {
        String jobName = param.getJobName();
        String jobGroup = param.getJobGroup();

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        checkJobExist(jobKey);

        // 先暂停再删除
        scheduler.pauseJob(jobKey);
        scheduler.deleteJob(jobKey);
    }

    @Override
    public List<QuartzJobDetailDto> jobList() throws SchedulerException {
        GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
        List<QuartzJobDetailDto> jobDtoList = new ArrayList<>();
        for (JobKey jobKey : scheduler.getJobKeys(matcher)) {
            QuartzJobDetailDto jobDto = getJobDtoByJobKey(jobKey);
            jobDtoList.add(jobDto);
        }
        return jobDtoList;
    }

    @Override
    public QuartzJobDetailDto jobDetail(QuartzDetailParam param) throws SchedulerException {
        String jobName = param.getJobName();
        String jobGroup = param.getJobGroup();
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        return getJobDtoByJobKey(jobKey);
    }

    private void checkJobExist(JobKey jobKey) throws SchedulerException {
        if (!scheduler.checkExists(jobKey)) {
            throw new BaseException("该定时任务不存在：" + jobKey.getName());
        }
    }

    public QuartzJobDetailDto getJobDtoByJobKey(JobKey jobKey) throws SchedulerException {
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        List<Trigger> triggerList = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);

        QuartzJobDetailDto jobDto = new QuartzJobDetailDto();
        jobDto.setJobClazz(jobDetail.getJobClass().toString());
        jobDto.setJobName(jobKey.getName());
        jobDto.setJobGroup(jobKey.getGroup());
        jobDto.setJobDataMap(jobDetail.getJobDataMap());

        List<QuartzTriggerDetailDto> triggerDtoList = new ArrayList<>();
        for (Trigger trigger : triggerList) {
            QuartzTriggerDetailDto triggerDto = new QuartzTriggerDetailDto();
            triggerDto.setTriggerName(trigger.getKey().getName());
            triggerDto.setTriggerGroup(trigger.getKey().getGroup());
            triggerDto.setDescription(trigger.getDescription());

            if (trigger instanceof CronTriggerImpl) {
                CronTriggerImpl cronTriggerImpl = (CronTriggerImpl) trigger;
                String cronExpression = cronTriggerImpl.getCronExpression();
                triggerDto.setCron(cronExpression);

                // 最近10次的触发时间
                List<Date> dates = TriggerUtils.computeFireTimes(cronTriggerImpl, null, 10);
                triggerDto.setRecentFireTimeList(dates);
            }

            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            triggerDto.setTriggerState(triggerState.toString());

            triggerDtoList.add(triggerDto);
        }
        jobDto.setTriggerDetailDtoList(triggerDtoList);
        return jobDto;
    }
}
