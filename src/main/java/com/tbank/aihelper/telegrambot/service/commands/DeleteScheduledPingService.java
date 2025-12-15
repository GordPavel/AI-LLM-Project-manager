package com.tbank.aihelper.telegrambot.service.commands;

import org.springframework.stereotype.Service;

import com.tbank.aihelper.telegrambot.dto.UpdateContext;
import com.tbank.aihelper.telegrambot.entity.JobBindingChat;
import com.tbank.aihelper.telegrambot.observer.EventListenerChatBot;
import com.tbank.aihelper.telegrambot.observer.ObserverChatBotAdapter;
import com.tbank.aihelper.telegrambot.service.CommandUtilsService;
import com.tbank.aihelper.telegrambot.service.JobBindingService;
import com.tbank.aihelper.telegrambot.service.ScheduledPingService;

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

    private final ScheduledPingService scheduledPingService;
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

        scheduledPingService.stopJob(jobId, updateContext.getChatId()); 
        commandUtils.fastSend(
            updateContext, 
            String.format("Пинг с номером #%s, успешно удалён", jobId)
        );
    } 
}
