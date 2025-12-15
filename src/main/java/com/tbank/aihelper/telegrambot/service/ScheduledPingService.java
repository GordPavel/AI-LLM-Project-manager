package com.tbank.aihelper.telegrambot.service;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tbank.aihelper.TaskTracker.TaskTrackerAdapter;
import com.tbank.aihelper.telegrambot.component.PingJob;
import com.tbank.aihelper.telegrambot.entity.TaskStatus;
import com.tbank.aihelper.telegrambot.entity.TgUser;
import com.tbank.aihelper.telegrambot.repository.JobBindingChatRepository;
import com.tbank.aihelper.telegrambot.repository.ResponseStatusTaskRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class ScheduledPingService {

    private static final String TASK_GROUP = "pingByTaskGroup";
    public static final String STATUS_TASK_COMPLETED = TaskStatus.Status.COMPLETED.name();
    
    private final Scheduler scheduler;
    private final ResponseStatusTaskRepository responseStatusTaskRepository;
    private final TaskTrackerAdapter taskTrackerAdapter;
    private final JobBindingChatRepository jobBindingChatRepository;

    public JobDetail createJob(String jobName, JobDataMap data) {
        return JobBuilder.newJob(PingJob.class)
                .withIdentity(jobName, TASK_GROUP)
                .usingJobData(data)
            .build();
    }

    public Trigger createTrigger(
        String trigerName, 
        CronScheduleBuilder cron, 
        ZonedDateTime endTime
    ) {
        return TriggerBuilder.newTrigger()
                .withIdentity("pingTrigger_" + trigerName, TASK_GROUP)
                .withSchedule(cron)
                .endAt(Date.from(endTime.toInstant()))
            .build();
    }

    public void startJob(JobDetail job, Trigger trigger, Long chatId) {
        try {
            scheduler.scheduleJob(job, trigger);
        } catch(SchedulerException e) {
            log.error("Error create ping: {}", e.getMessage());
            throw new com.tbank.aihelper.telegrambot.exception.SchedulerException(
                "Error create ping: {}" + e.getMessage(),
                "Не удалось создать пинг",
                chatId
            );
        }
    }

    public void stopJob(String jobId, Long chatId) {
        try {
            scheduler.deleteJob(new JobKey(jobId, TASK_GROUP));
        } catch(SchedulerException e) {
            log.error("Error create ping: {}", e.getMessage());
            throw new com.tbank.aihelper.telegrambot.exception.SchedulerException(
                "Error delete ping: {}" + e.getMessage(),
                "Не удалось удалить пинг",
                chatId
            );
        }
    }

    @Transactional
    public List<TgUser> getDoneExecutors(Long jobId) {
        return responseStatusTaskRepository.findLatestByJobId(jobId).stream()
                .filter(rst -> rst.getIdentifiedLLMStatus().getStatus().name() == STATUS_TASK_COMPLETED)
                .map(obj -> obj.getUser())
            .toList();
    }

    @Transactional
    public List<TgUser> getNotDoneExecutors(Long jobId, Set<TgUser> executorsDone, Long chatId) {
        List<TgUser> allExecutors = jobBindingChatRepository.findById(jobId)
            .orElseThrow().getPerformers();
        return allExecutors.stream()
                .filter(user -> !executorsDone.contains(user))
            .toList();
    }

    public void setTaskDone(String jobName, String taskId, Long chatId) {
        stopJob(jobName, chatId);
        taskTrackerAdapter.transitionTask(taskId, STATUS_TASK_COMPLETED);
    }
}
