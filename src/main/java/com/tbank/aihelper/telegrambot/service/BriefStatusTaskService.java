//package com.tbank.aihelper.telegrambot.service;
//
//import java.util.List;
//import java.util.Optional;
//
//import com.tbank.aihelper.llm.OpenRouterGrokAdapter;
//import com.tbank.aihelper.llm.dto.ChatCompletionRequest;
//import com.tbank.aihelper.llm.dto.ChatCompletionResponse;
//import com.tbank.aihelper.llm.dto.ChatMessage;
//import com.tbank.aihelper.llm.dto.Choice;
//import org.springframework.stereotype.Service;
//
//import com.tbank.aihelper.llm.LLMAdapter;
//import com.tbank.aihelper.telegrambot.ChatBotAdapter;
//import com.tbank.aihelper.telegrambot.dto.BotMessage;
//import com.tbank.aihelper.telegrambot.dto.UpdateContext;
//import com.tbank.aihelper.telegrambot.entity.ChatConfiguration;
//import com.tbank.aihelper.telegrambot.observer.EventListenerChatBot;
//import com.tbank.aihelper.telegrambot.observer.ObserverChatBotAdapter;
//import com.tbank.aihelper.telegrambot.repository.ChatConfigurationRepository;
//
//import jakarta.annotation.PostConstruct;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Service
//@AllArgsConstructor
//public class BriefStatusTaskService implements EventListenerChatBot {
//
//    private static final String HANDLE_COMMAND = "/status_task";
//
//    private final ChatBotAdapter chatBotAdapter;
//    private final ChatConfigurationRepository chatConfigurationRepository;
//    private final ObserverChatBotAdapter observerChatBotAdapter;
////    private final LLMAdapter llmAdapter;
//    private final OpenRouterGrokAdapter openRouterGrokAdapter;
//    @PostConstruct
//    private void subscribeToObserver() {
//        observerChatBotAdapter.subscribe(this);
//    }
//
//    public void handleNewMessage(UpdateContext updateContext) {
//
//        if (updateContext.getCommand() == null
//                || !updateContext.getCommand().equals(HANDLE_COMMAND)) {
//            return;
//        }
//
//        String userPrompt = updateContext.getTextMessage();
//
//        if (userPrompt == null || userPrompt.trim().isEmpty()) {
//            chatBotAdapter.sendMessage(BotMessage.builder()
//                    .chatId(updateContext.getChatId())
//                    .replyToMessageId(updateContext.getMessageId())
//                    .textMessage("Укажите текст задачи после команды /status_task.")
//                    .build());
//            return;
//        }
//
//        Optional<ChatConfiguration> oConfig = chatConfigurationRepository.findByChatId(updateContext.getChatId());
//        if (oConfig.isEmpty()) {
//            chatBotAdapter.sendMessage(BotMessage.builder()
//                    .chatId(updateContext.getChatId())
//                    .replyToMessageId(updateContext.getMessageId())
//                    .textMessage("Конфигурация бота не найдена.")
//                    .build());
//            return;
//        }
//
//        ChatConfiguration config = oConfig.get();
//
//        // 👍 Улучшенный промпт (контекст + правила)
//        String systemPrompt = """
//            Ты — помощник, который помогает разбирать задачи разработки.
//
//            Контекст:
//            %s
//
//            Правила:
//            - если пользователь спрашивает про задачу, попробуй извлечь её идентификатор
//            - вызови инструмент get_trello_task чтобы получить данные задачи
//            - на основе данных дай полезное описание человеческим языком, например:
//              резюме, цель задачи, текущий статус, исполнитель
//            - если данных недостаточно — скажи об этом
//            - если задачу не нашли — скажи: "Задача <id> не найдена"
//
//            Формат ответа:
//            - коротко, 3–7 предложений
//            - без JSON
//            - без лишнего мусора
//            """.formatted(config.getConfiguration());
//
//        ChatCompletionRequest request = ChatCompletionRequest.builder()
//                .model("amazon/nova-2-lite-v1:free")
//                .messages(List.of(
//                        new ChatMessage("system", systemPrompt),
//                        new ChatMessage("user", userPrompt)
//                ))
//                .tools(openRouterGrokAdapter.getDefaultTools())
//                .temperature(0.2)
//                .maxTokens(512)
//                .build();
//
//        try {
//            ChatCompletionResponse response = openRouterGrokAdapter.chatCompletion(request);
//
//            String finalAnswer = extractAnswer(response);
//
//            if (finalAnswer == null || finalAnswer.isBlank()) {
//                finalAnswer = "Ответ не получен. Попробуйте сформулировать запрос иначе.";
//            }
//
//            chatBotAdapter.sendMessage(BotMessage.builder()
//                    .chatId(updateContext.getChatId())
//                    .replyToMessageId(updateContext.getMessageId())
//                    .textMessage(finalAnswer)
//                    .build());
//
//        } catch (Exception e) {
//            log.error("Ошибка при обращении к LLM", e);
//
//            chatBotAdapter.sendMessage(BotMessage.builder()
//                    .chatId(updateContext.getChatId())
//                    .replyToMessageId(updateContext.getMessageId())
//                    .textMessage("Произошла ошибка при обработке запроса: " + e.getMessage())
//                    .build());
//        }
//    }
//
//
//    private String extractAnswer(ChatCompletionResponse response) {
//        if (response == null || response.getChoices() == null) {
//            return null;
//        }
//
//        for (Choice choice : response.getChoices()) {
//            ChatMessage msg = choice.getMessage();
//            if (msg == null) continue;
//
//            String c = msg.getContent();
//            if (c != null && !c.trim().isEmpty()) {
//                return c.trim();
//            }
//        }
//
//        return null;
//    }
//
//
//}
package com.tbank.aihelper.telegrambot.service;

import java.util.List;
import java.util.Optional;

import com.tbank.aihelper.llm.OpenRouterGrokAdapter;
import com.tbank.aihelper.llm.dto.ChatCompletionRequest;
import com.tbank.aihelper.llm.dto.ChatCompletionResponse;
import com.tbank.aihelper.llm.dto.ChatMessage;
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
public class BriefStatusTaskService implements EventListenerChatBot {

    private static final String HANDLE_COMMAND = "/status_task";

    private final ChatBotAdapter chatBotAdapter;
    private final ChatConfigurationRepository chatConfigurationRepository;
    private final ObserverChatBotAdapter observerChatBotAdapter;
    private final OpenRouterGrokAdapter openRouterGrokAdapter;

    @PostConstruct
    private void subscribeToObserver() {
        observerChatBotAdapter.subscribe(this);
    }

    @Override
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
                    .textMessage("Конфигурация бота для этого чата не найдена.")
                    .build());
            return;
        }

        ChatConfiguration config = oConfig.get();

        // Формируем запрос к LLM с контекстом конфигурации
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("amazon/nova-2-lite-v1:free")
                .messages(List.of(
                        new ChatMessage("system", buildSystemPrompt(config.getConfiguration())),
                        new ChatMessage("user", updateContext.getTextMessage())
                ))
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

            // Страховка на пустой ответ
            if (finalAnswer == null || finalAnswer.trim().isEmpty()) {
                finalAnswer = "ЛЛМ вернула пустой ответ. Проверьте, задан ли контекст задачи через /set_config.";
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

    private String buildSystemPrompt(String config) {
        return """
                Ты — помощник, который помогает разбирать задачи разработки.

                Контекст активной задачи:
                %s

                Правила:
                - Используй только данные из этого контекста.
                - Если информации недостаточно — скажи об этом.
                - Отвечай коротко, 3–7 предложений, понятным языком, без JSON и лишнего мусора.
                """.formatted(config);
    }
}
