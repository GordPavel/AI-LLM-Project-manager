package com.tbank.aihelper.telegrambot.service.commands;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import com.tbank.aihelper.telegrambot.dto.UpdateContext;
import com.tbank.aihelper.telegrambot.entity.JobBindingChat;
import com.tbank.aihelper.telegrambot.observer.EventListenerChatBot;
import com.tbank.aihelper.telegrambot.observer.ObserverChatBotAdapter;
import com.tbank.aihelper.telegrambot.service.CommandUtilsService;
import com.tbank.aihelper.telegrambot.service.JobBindingService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
    /delete_ping 
    Params:
        ping_id: String
*/
@Slf4j
@Service
@AllArgsConstructor
public class DeleteScheduledPingService implements EventListenerChatBot {
    
    private static final String HANDLE_COMMAND = "/delete_ping";

    private final Scheduler scheduler;
    private final JobBindingService jobBindingService;
    private final ObserverChatBotAdapter observerChatBotAdapter;
    private final CommandUtilsService commandUtils;

    @PostConstruct
    private void subscribeToObserver() {
        observerChatBotAdapter.subscribe(this);
    }

    public void handleNewMessage(UpdateContext updateContext) {
        if(!commandUtils.shouldHandle(updateContext, HANDLE_COMMAND))
            return;
        commandUtils.ensureNonEmptyMessage(updateContext, HANDLE_COMMAND);   

        JobBindingChat jobBindChat = jobBindingService.getByJobId(
            updateContext.getTextMessage(),
            updateContext.getChatId()
        );
        if(!jobBindChat.getChatConfiguration().getChatId().equals(updateContext.getChatId()))
            return;
        String jobId = jobBindChat.getId().toString();

        try {
            jobBindingService.deleteJobById(Long.valueOf(jobId));
            scheduler.deleteJob(new JobKey(jobId, "pingByTaskGroup"));

            commandUtils.fastSend(
                updateContext, 
                String.format("Пинг с номером #%s, успешно удалён", jobId)
            );
        } catch(SchedulerException e) {
            log.error("Error delete ping: {}", e.getMessage());
            
            commandUtils.fastSend(
                updateContext, 
                "Не удалось удалить пинг"
            );
        }
    } 
}
