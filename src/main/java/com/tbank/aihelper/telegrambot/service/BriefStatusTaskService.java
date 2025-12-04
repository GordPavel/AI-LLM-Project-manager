package com.tbank.aihelper.telegrambot.service;

import java.util.List;
import java.util.Optional;

import com.tbank.aihelper.llm.OpenRouterGrokAdapter;
import com.tbank.aihelper.llm.dto.ChatCompletionRequest;
import com.tbank.aihelper.llm.dto.ChatCompletionResponse;
import com.tbank.aihelper.llm.dto.ChatMessage;
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
//    private final LLMAdapter llmAdapter;
    private final OpenRouterGrokAdapter openRouterGrokAdapter;
    @PostConstruct
    private void subscribeToObserver() {
        observerChatBotAdapter.subscribe(this);
    }

    public void handleNewMessage(UpdateContext updateContext) {

        if (updateContext.getCommand() == null
                || !updateContext.getCommand().equals(HANDLE_COMMAND)) {
            return;
        }

        if (updateContext.getTextMessage() == null
                || updateContext.getTextMessage().trim().isEmpty()) {
            chatBotAdapter.sendMessage(BotMessage.builder()
                    .chatId(updateContext.getChatId())
                    .replyToMessageId(updateContext.getMessageId())
                    .textMessage("Укажите текст задачи после команды /status_task.")
                    .build());
            return;
        }

        Optional<ChatConfiguration> oConfig = chatConfigurationRepository.findByChatId(updateContext.getChatId());
        if (oConfig.isEmpty()) {
            chatBotAdapter.sendMessage(BotMessage.builder()
                    .chatId(updateContext.getChatId())
                    .replyToMessageId(updateContext.getMessageId())
                    .textMessage("Конфигурация бота не найдена.")
                    .build());
            return;
        }

        ChatConfiguration config = oConfig.get();

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("amazon/nova-2-lite-v1:free")
                .messages(List.of(
                        new ChatMessage("system", config.getConfiguration()),
                        new ChatMessage("user", updateContext.getTextMessage())
                ))
                .tools(openRouterGrokAdapter.getDefaultTools())
                .temperature(0.3)
                .maxTokens(1000)
                .build();

        try {
            ChatCompletionResponse response = openRouterGrokAdapter.chatCompletion(request);

            String finalAnswer = null;

            if (response != null &&
                    response.getChoices() != null &&
                    !response.getChoices().isEmpty() &&
                    response.getChoices().get(0).getMessage() != null) {

                finalAnswer = response.getChoices().get(0).getMessage().getContent();
            }

            // ⚠️ Страховка: если LLM вернула null, "", "     "
            if (finalAnswer == null || finalAnswer.trim().isEmpty()) {
                finalAnswer = "ЛЛМ вернула пустой ответ.";
            }

            chatBotAdapter.sendMessage(BotMessage.builder()
                    .chatId(updateContext.getChatId())
                    .replyToMessageId(updateContext.getMessageId())
                    .textMessage(finalAnswer)
                    .build());

        } catch (Exception e) {
            log.error("Ошибка при обращении к LLM", e);

            chatBotAdapter.sendMessage(BotMessage.builder()
                    .chatId(updateContext.getChatId())
                    .replyToMessageId(updateContext.getMessageId())
                    .textMessage("Произошла ошибка при обработке запроса: " + e.getMessage())
                    .build());
        }
    }

}
