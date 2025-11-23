package com.tbank.aihelper.telegrambot.service;

import org.springframework.stereotype.Service;

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
public class ConfigurateBotService implements EventListenerChatBot {
    
    private static final String HANDLE_COMMAND = "/set_config";
    
    private final ChatBotAdapter chatBotAdapter;
    private final ChatConfigurationRepository chatConfigurationRepository;
    private final ObserverChatBotAdapter observerChatBotAdapter;

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

        chatConfigurationRepository.save(
            ChatConfiguration.builder()
                    .chatId(updateContext.getChatId())
                    .configuration(updateContext.getTextMessage())
                .build()
        );

        chatBotAdapter.sendMessage(BotMessage.builder()
                .chatId(updateContext.getChatId())
                .replyToMessageId(updateContext.getMessageId())
                .textMessage("Конфигурация бота для чата была сохранена.")
            .build());
    }
}
