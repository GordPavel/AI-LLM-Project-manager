package com.tbank.aihelper.telegrambot.service.commands;

import org.springframework.stereotype.Service;

import com.tbank.aihelper.llm.LLMAdapter;
import com.tbank.aihelper.telegrambot.dto.UpdateContext;
import com.tbank.aihelper.telegrambot.entity.ChatConfiguration;
import com.tbank.aihelper.telegrambot.observer.EventListenerChatBot;
import com.tbank.aihelper.telegrambot.observer.ObserverChatBotAdapter;
import com.tbank.aihelper.telegrambot.service.CommandUtilsService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class BriefStatusTaskService implements EventListenerChatBot {
    
    private static final String HANDLE_COMMAND = "/status_task";

    private final ConfigurateBotService configurateBotService;
    private final ObserverChatBotAdapter observerChatBotAdapter;
    private final LLMAdapter llmAdapter;
    private final CommandUtilsService commandUtils;

    @PostConstruct
    private void subscribeToObserver() {
        observerChatBotAdapter.subscribe(this);
    }

    public void handleNewMessage(UpdateContext updateContext) {
        if(!commandUtils.shouldHandle(updateContext, HANDLE_COMMAND))
            return;
        commandUtils.ensureNonEmptyMessage(updateContext, HANDLE_COMMAND);   

        ChatConfiguration chatConfig = configurateBotService.getChatConfig(updateContext.getChatId());

        String llmAnswer = llmAdapter.ask(
            "Контекст: " + chatConfig.getConfiguration() +
            "Вопрос: " + updateContext.getTextMessage()
        );
        
        commandUtils.fastSend(updateContext, llmAnswer);
    }
}
