package com.tbank.aihelper.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbank.aihelper.TaskTracker.TrelloTrackerAdapterImpl;
import com.tbank.aihelper.llm.config.OpenRouterProperties;
import com.tbank.aihelper.llm.dto.*;
import com.tbank.aihelper.llm.exception.EmptyResponseException;
import com.tbank.aihelper.llm.exception.LlmExternalApiException;
import com.tbank.aihelper.llm.exception.ParsingResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenRouterGrokAdapter implements LLMAdapter {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final TrelloTrackerAdapterImpl trelloAdapter;
    private final OpenRouterProperties properties;

    @Override
    public ChatCompletionResponse chatCompletion(ChatCompletionRequest request) {
        String model = request.getModel() != null ? request.getModel() : properties.getModel();
        List<ChatMessage> messages = new ArrayList<>(request.getMessages());
        List<Tool> tools = request.getTools() != null ? request.getTools() : getDefaultTools();

        int maxIterations = 12;
        ChatCompletionResponse lastResponse = null;

        for (int i = 0; i < maxIterations; i++) {
            Map<String, Object> body = buildRequestBody(model, messages, request, tools);
            HttpHeaders headers = buildHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response;
            try {
                response = restTemplate.exchange(
                        properties.getBaseUrl() + "/chat/completions",
                        HttpMethod.POST,
                        entity,
                        Map.class
                );
            } catch (Exception e) {
                throw new LlmExternalApiException("Ошибка соединения с OpenRouter", e);
            }

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new EmptyResponseException();
            }

            try {
                lastResponse = mapper.convertValue(responseBody, ChatCompletionResponse.class);
            } catch (Exception e) {
                throw new ParsingResponseException(e);
            }

            List<Choice> choices = lastResponse.getChoices();
            if (choices == null || choices.isEmpty()) break;

            ChatMessage assistantMessage = choices.get(0).getMessage();
            messages.add(assistantMessage);

            if (assistantMessage.getToolCalls() == null || assistantMessage.getToolCalls().isEmpty()) {
                break;
            }

            for (ToolCall toolCall : assistantMessage.getToolCalls()) {
                ToolResponse toolResult = executeToolSafely(toolCall);
                String content = serializeToolResponse(toolResult);
                messages.add(createToolMessage(toolCall.getId(), content));
            }
        }

        return Objects.requireNonNull(lastResponse, "LLM не вернула финальный ответ");
    }

    private ToolResponse executeToolSafely(ToolCall toolCall) {
        try {
            return executeTool(toolCall.getFunction().getName(), toolCall.getFunction().getArguments());
        } catch (Exception e) {
            log.error("Ошибка выполнения инструмента: {}", toolCall.getFunction().getName(), e);
            return ToolResponse.error("Не удалось выполнить действие: " + e.getMessage());
        }
    }

    private String serializeToolResponse(ToolResponse response) {
        try {
            return mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return mapper.createObjectNode()
                    .put("type", "error")
                    .put("error", "Ошибка сериализации ответа")
                    .toString();
        }
    }

    private ChatMessage createToolMessage(String toolCallId, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setRole("tool");
        msg.setContent(content);
        msg.setToolCallId(toolCallId);
        return msg;
    }

    private ToolResponse executeTool(String name, String argsJson) throws Exception {
        JsonNode args = mapper.readTree(argsJson);

        return switch (name) {
            case "get_trello_task" -> {
                String key = args.get("key").asText();
                Object task = trelloAdapter.getTask(key)
                        .orElseThrow(() -> new RuntimeException("Задача не найдена: " + key));
                yield ToolResponse.task(task);
            }
            case "create_trello_task" -> {
                String summary = args.get("summary").asText();
                String description = args.has("description") ? args.get("description").asText() : "";
                String assignee = args.has("assignee") ? args.get("assignee").asText() : "";
                Object task = trelloAdapter.createTask("To Do", summary, description, assignee);
                yield ToolResponse.task(task);
            }
            case "update_trello_task_status" -> {
                String key = args.get("key").asText();
                String status = args.get("status").asText();
                Object task = trelloAdapter.transitionTask(key, status);
                yield ToolResponse.task(task);
            }
            case "add_comment_to_task" -> {
                String key = args.get("key").asText();
                String text = args.get("text").asText();
                Object comment = trelloAdapter.addComment(key, text, "AI Assistant");
                yield ToolResponse.comment(comment);
            }
            default -> throw new IllegalArgumentException("Неизвестный инструмент: " + name);
        };
    }

    private Map<String, Object> buildRequestBody(String model, List<ChatMessage> messages,
                                                 ChatCompletionRequest request, List<Tool> tools) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        if (request.getTemperature() != null) body.put("temperature", request.getTemperature());
        if (request.getMaxTokens() != null) body.put("max_tokens", request.getMaxTokens());
        if (!tools.isEmpty()) body.put("tools", tools);
        return body;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + properties.getApiKey());
        headers.set("HTTP-Referer", properties.getReferer());
        headers.set("X-Title", properties.getTitle());
        return headers;
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

    private Tool createTool(String name, String desc, Map<String, String> propsMap, List<String> required) {
        Map<String, Property> props = new HashMap<>();
        propsMap.forEach((k, v) -> props.put(k, new Property("string", v)));
        return new Tool(new FunctionDefinition(name, desc, new Parameters(props, required)));
    }
}