package com.tbank.aihelper.telegrambot.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import com.tbank.aihelper.telegrambot.ChatBotAdapter;
import com.tbank.aihelper.telegrambot.dto.BotMessage;
import com.tbank.aihelper.telegrambot.dto.SetPingDto;

@Slf4j
@Component
@NoArgsConstructor
public class PingJob implements Job {

    @Autowired
    private ChatBotAdapter chatBotAdapter;
    
    @Override
    public void execute(JobExecutionContext context) {
        SetPingDto data = (SetPingDto) context.getMergedJobDataMap().get("setPingDto");
        log.debug("{}", data);

        chatBotAdapter.sendMessage(BotMessage.builder()
                .chatId(data.getChatId())
                .replyToMessageId(data.getMessageId())
                .textMessage(String.format(
                    "Уточните статусы по задаче #%s\n%s", 
                    data.getTaskId(), String.join(", ", data.getUsernamesToPing()))
                )
            .build());
    }
}
