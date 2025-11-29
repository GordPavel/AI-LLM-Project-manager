package com.tbank.aihelper.telegrambot.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tbank.aihelper.llm.LLMAdapter;
import com.tbank.aihelper.telegrambot.ChatBotAdapter;
import com.tbank.aihelper.telegrambot.dto.BotMessage;
import com.tbank.aihelper.telegrambot.dto.UpdateContext;
import com.tbank.aihelper.telegrambot.entity.ChatConfiguration;
import com.tbank.aihelper.telegrambot.observer.EventListenerChatBot;
import com.tbank.aihelper.telegrambot.observer.ObserverChatBotAdapter;
import com.tbank.aihelper.telegrambot.repository.ChatConfigurationRepository;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class BriefStatusTaskService implements EventListenerChatBot {
    
    private static final String HANDLE_COMMAND = "/status_task";

    private final ChatBotAdapter chatBotAdapter;
    private final ChatConfigurationRepository chatConfigurationRepository;
    private final ObserverChatBotAdapter observerChatBotAdapter;
    private final LLMAdapter llmAdapter;

    @PostConstruct
    private void subscribeToObserver() {
        observerChatBotAdapter.subscribe(this);
    }

    public void handleNewMessage(UpdateContext updateContext) {
        
        if(updateContext.getCommand() == null 
            || !updateContext.getCommand().equals(HANDLE_COMMAND))
            return;

        if(updateContext.getTextMessage() == null 
            || updateContext.getTextMessage().isEmpty())
            return;
        
        Optional<ChatConfiguration> oAnswer = chatConfigurationRepository.findByChatId(updateContext.getChatId());
        if(oAnswer.isEmpty()) {
            chatBotAdapter.sendMessage(BotMessage.builder()
                .chatId(updateContext.getChatId())
                .replyToMessageId(updateContext.getMessageId())
                .textMessage("Конфигурация бота не найдена.")
            .build());
            return;
        }

        String llmAnswer = llmAdapter.ask(
            "Контекст: " + oAnswer.get().getConfiguration() +
            "Вопрос: " + updateContext.getTextMessage()
        );
        
        chatBotAdapter.sendMessage(BotMessage.builder()
                .chatId(updateContext.getChatId())
                .replyToMessageId(updateContext.getMessageId())
                .textMessage(llmAnswer)
            .build());
    }
}
