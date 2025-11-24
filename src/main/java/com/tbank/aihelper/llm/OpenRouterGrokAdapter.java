package com.tbank.aihelper.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbank.aihelper.TaskTracker.TrelloTrackerAdapterImpl;
import com.tbank.aihelper.llm.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OpenRouterGrokAdapter implements LLMAdapter {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final TrelloTrackerAdapterImpl trelloAdapter;

    @Value("${llm.openrouter.api-key}")
    private String apiKey;

    @Value("${llm.openrouter.model:x-ai/grok-4.1-fast}")
    private String defaultModel;

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";


    @Override
    public ChatCompletionResponse chatCompletion(ChatCompletionRequest request) {
        String model = request.getModel() != null ? request.getModel() : defaultModel;
        List<ChatMessage> messages = new ArrayList<>(request.getMessages());
        List<Tool> tools = request.getTools() != null ? request.getTools() : getDefaultTools();

        int maxIterations = 10;
        ChatCompletionResponse lastResponse = null;

        for (int i = 0; i < maxIterations; i++) {
            // Подготовка тела запроса
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            if (request.getTemperature() != null) body.put("temperature", request.getTemperature());
            if (request.getMaxTokens() != null) body.put("max_tokens", request.getMaxTokens());
            if (tools != null && !tools.isEmpty()) {
                body.put("tools", tools);
            }

            // Заголовки
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("HTTP-Referer", "http://localhost:8080");  // обязательно для OpenRouter
            headers.set("X-Title", "AI Project Manager");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // Отправка
            ResponseEntity<Map> response = restTemplate.exchange(
                    OPENROUTER_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Пустой ответ от OpenRouter");
            }

            lastResponse = mapper.convertValue(responseBody, ChatCompletionResponse.class);

            List<Choice> choices = lastResponse.getChoices();
            if (choices == null || choices.isEmpty()) break;

            ChatMessage assistantMessage = choices.get(0).getMessage();
            messages.add(assistantMessage);

            // Если нет tool_calls — это финальный ответ
            if (assistantMessage.getToolCalls() == null || assistantMessage.getToolCalls().isEmpty()) {
                break;
            }

            // Обработка всех tool calls
            for (ToolCall toolCall : assistantMessage.getToolCalls()) {
                String toolCallId = toolCall.getId();
                String functionName = toolCall.getFunction().getName();
                String argsJson = toolCall.getFunction().getArguments();

                String resultJson;
                try {
                    resultJson = executeTool(functionName, argsJson);
                } catch (Exception e) {
                    resultJson = "{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
                }

                ChatMessage toolResponse = new ChatMessage();
                toolResponse.setRole("tool");
                toolResponse.setContent(resultJson);
                toolResponse.setToolCallId(toolCallId);
                messages.add(toolResponse);
            }
        }

        return lastResponse;
    }

    private String executeTool(String name, String argsJson) throws Exception {
        JsonNode args = mapper.readTree(argsJson);

        return switch (name) {
            case "get_trello_task" -> {
                String key = args.get("key").asText();
                var task = trelloAdapter.getTask(key)
                        .orElseThrow(() -> new RuntimeException("Задача не найдена: " + key));
                yield mapper.writeValueAsString(task);
            }
            case "create_trello_task" -> {
                String summary = args.get("summary").asText();
                String description = args.has("description") ? args.get("description").asText() : "";
                String assignee = args.has("assignee") ? args.get("assignee").asText() : "";
                var task = trelloAdapter.createTask("To Do", summary, description, assignee);
                yield mapper.writeValueAsString(task);
            }
            case "update_trello_task_status" -> {
                String key = args.get("key").asText();
                String status = args.get("status").asText();
                var task = trelloAdapter.transitionTask(key, status);
                yield mapper.writeValueAsString(task);
            }
            case "add_comment_to_task" -> {
                String key = args.get("key").asText();
                String text = args.get("text").asText();
                var comment = trelloAdapter.addComment(key, text, "AI Assistant");
                yield mapper.writeValueAsString(comment);
            }
            default -> throw new IllegalArgumentException("Неизвестный инструмент: " + name);
        };
    }

    @Override
    public List<Tool> getDefaultTools() {
        List<Tool> tools = new ArrayList<>();

        // 1. Получить задачу
        tools.add(new Tool(new FunctionDefinition(
                "get_trello_task",
                "Получить полную информацию о задаче в Trello по её ключу (ID карточки)",
                new Parameters(
                        Map.of("key", new Property("string", "ID карточки в Trello, например: 67a1b2c3d4e5f6g7h8i9j0k1")),
                        List.of("key")
                )
        )));

        // 2. Создать задачу
        tools.add(new Tool(new FunctionDefinition(
                "create_trello_task",
                "Создать новую задачу в колонке To Do",
                new Parameters(
                        Map.of(
                                "summary", new Property("string", "Краткое название задачи"),
                                "description", new Property("string", "Подробное описание (опционально)"),
                                "assignee", new Property("string", "ID участника Trello (опционально)")
                        ),
                        List.of("summary")
                )
        )));

        // 3. Перевести задачу в другой статус
        tools.add(new Tool(new FunctionDefinition(
                "update_trello_task_status",
                "Перевести задачу в другой статус (In Progress, Review, Done и т.д.)",
                new Parameters(
                        Map.of(
                                "key", new Property("string", "ID задачи"),
                                "status", new Property("string", "Новый статус: To Do, In Progress, Review, Done и т.д.")
                        ),
                        List.of("key", "status")
                )
        )));

        // 4. Добавить комментарий
        tools.add(new Tool(new FunctionDefinition(
                "add_comment_to_task",
                "Добавить комментарий к задаче от имени AI",
                new Parameters(
                        Map.of(
                                "key", new Property("string", "ID задачи"),
                                "text", new Property("string", "Текст комментария")
                        ),
                        List.of("key", "text")
                )
        )));

        return tools;
    }
}
