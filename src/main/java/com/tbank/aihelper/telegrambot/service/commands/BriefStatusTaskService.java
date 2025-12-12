package com.tbank.aihelper.telegrambot.service.commands;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tbank.aihelper.TaskTracker.TaskItem;
import com.tbank.aihelper.TaskTracker.TrelloTrackerAdapterImpl;
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
    private final OpenRouterGrokAdapter llmAdapter;
    private final TrelloTrackerAdapterImpl trelloAdapter;

    @PostConstruct
    private void subscribeToObserver() {
        observerChatBotAdapter.subscribe(this);
    }

    @Override
    public void handleNewMessage(UpdateContext updateContext) {
        if (!HANDLE_COMMAND.equals(updateContext.getCommand())) {
            return;
        }

        String question = updateContext.getTextMessage();
        if (question == null || question.trim().isEmpty()) {
            send("Напишите вопрос по задаче после команды /status_task", updateContext);
            return;
        }

        Optional<ChatConfiguration> configOpt = chatConfigurationRepository.findByChatId(updateContext.getChatId());
        if (configOpt.isEmpty()) {
            send("Конфигурация чата не найдена. Используйте /set_config <card_id>", updateContext);
            return;
        }

        String cardId = configOpt.get().getConfiguration().trim();


        Optional<TaskItem> taskOpt = trelloAdapter.getTask(cardId);
        if (taskOpt.isEmpty()) {
            send("Задача с ID " + cardId + " не найдена в Trello.", updateContext);
            return;
        }

        TaskItem task = taskOpt.get();


        String taskContext = """
                Активная задача:
                Название: %s
                Описание: %s
                Статус: %s
                Исполнитель: %s
                Ссылка: %s
                Комментарии:
                %s
                """.formatted(
                task.getSummary(),
                task.getDescription() != null ? task.getDescription() : "—",
                task.getStatus(),
                task.getAssignee() != null ? task.getAssignee() : "не назначен",
                task.getUrl(),
                task.getComments().isEmpty()
                        ? "— нет комментариев"
                        : task.getComments().stream()
                        .map(c -> "• %s: %s".formatted(c.getAuthor(), c.getText()))
                        .collect(Collectors.joining("\n"))
        );


        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("amazon/nova-2-lite-v1:free")
                .messages(List.of(
                        new ChatMessage("system", """
                                Ты — помощник по задачам в Trello.
                                Вот полная информация по текущей задаче:
                                %s
                                
                                Отвечай кратко, по-русски, понятно и по делу.
                                Используй только данные выше.
                                """.formatted(taskContext)),
                        new ChatMessage("user", question)
                ))
                .temperature(0.3)
                .maxTokens(1000)
                .build();

        try {
            ChatCompletionResponse response = llmAdapter.chatCompletion(request);
            String answer = response.getChoices().get(0).getMessage().getContent();

            send(answer != null ? answer : "Не удалось получить ответ от ИИ.", updateContext);

        } catch (Exception e) {
            log.error("Ошибка при обращении к LLM", e);
            send("Произошла ошибка при анализе задачи: " + e.getMessage(), updateContext);
        }
    }

    private void send(String text, UpdateContext ctx) {
        chatBotAdapter.sendMessage(BotMessage.builder()
                .chatId(ctx.getChatId())
                .replyToMessageId(ctx.getMessageId())
                .textMessage(text)
                .build());
    }
}