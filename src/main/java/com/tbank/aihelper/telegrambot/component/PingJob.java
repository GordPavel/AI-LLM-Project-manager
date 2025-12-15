package com.tbank.aihelper.telegrambot.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import com.tbank.aihelper.telegrambot.ChatBotAdapter;
import com.tbank.aihelper.telegrambot.dto.BotMessage;
import com.tbank.aihelper.telegrambot.dto.SetPingDto;
import com.tbank.aihelper.telegrambot.service.ScheduledPingService;

@Slf4j
@Component
@NoArgsConstructor
public class PingJob implements Job {

    @Autowired
    private ChatBotAdapter chatBotAdapter;

    @Autowired
    private ScheduledPingService scheduledPingService;
    
    @Override
    public void execute(JobExecutionContext context) {
        SetPingDto data = (SetPingDto) context.getMergedJobDataMap().get("setPingDto");
        String jobName = (String) context.getMergedJobDataMap().get("setJobName");
        log.debug("{}", data);

        List<String> usernamesDontDone = scheduledPingService.getNotDoneExecutors(
            Long.valueOf(jobName), 
            new HashSet<>(scheduledPingService.getDoneExecutors(Long.valueOf(jobName))),
            data.getChatId()
        ).stream().map(obj -> obj.getUsername()).toList();

        if(!usernamesDontDone.isEmpty()) {
            chatBotAdapter.sendMessage(BotMessage.builder()
                    .chatId(data.getChatId())
                    .replyToMessageId(data.getMessageId())
                    .textMessage(String.format(
                        "Уточните статусы по задаче #%s\n%s", 
                        data.getTaskId(), String.join(", ", usernamesDontDone.stream().map(u -> "@" + u).toList()))
                    )
                .build());
        }
    }
}
