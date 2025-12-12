package com.tbank.aihelper.telegrambot.service.commands;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tbank.aihelper.telegrambot.dto.UpdateContext;
import com.tbank.aihelper.telegrambot.entity.ChatConfiguration;
import com.tbank.aihelper.telegrambot.exception.NotFoundException;
import com.tbank.aihelper.telegrambot.observer.EventListenerChatBot;
import com.tbank.aihelper.telegrambot.observer.ObserverChatBotAdapter;
import com.tbank.aihelper.telegrambot.repository.ChatConfigurationRepository;
import com.tbank.aihelper.telegrambot.service.CommandUtilsService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
    /set_config
    Params:
        config: String
*/
@Slf4j
@Service
@AllArgsConstructor
public class ConfigurateBotService implements EventListenerChatBot {
    
    private static final String HANDLE_COMMAND = "/set_config";
    
    private final ChatConfigurationRepository chatConfigurationRepository;
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

        Optional<ChatConfiguration> opChatConf = chatConfigurationRepository.findByChatId(updateContext.getChatId());
        if(opChatConf.isPresent()) {
            ChatConfiguration chatConf = opChatConf.get();
            chatConf.setConfiguration(updateContext.getTextMessage());
            chatConfigurationRepository.save(chatConf);
        } else {
            chatConfigurationRepository.save(
                ChatConfiguration.builder()
                        .chatId(updateContext.getChatId())
                        .configuration(updateContext.getTextMessage())
                    .build()
            );
        }

        commandUtils.fastSend(
            updateContext, 
            "Конфигурация бота для чата была сохранена."
        );
    }

    public ChatConfiguration getChatConfig(Long chatId) throws NotFoundException {
        return chatConfigurationRepository.findByChatId(chatId)
            .orElseThrow(() -> new NotFoundException(
                    "Not found 'ChatConfiguration' with id: '" + chatId.toString() + "' in database.",
                    "Не удалось найти конфигурацию для этого чата.",
                    chatId
                ));
    }
}
