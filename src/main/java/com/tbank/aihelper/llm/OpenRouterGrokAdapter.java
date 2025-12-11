package com.tbank.aihelper.llm;

import com.tbank.aihelper.llm.dto.ChatCompletionRequest;
import com.tbank.aihelper.llm.dto.ChatCompletionResponse;
import com.tbank.aihelper.llm.dto.ChatMessage;
import com.tbank.aihelper.llm.exception.EmptyResponseException;
import com.tbank.aihelper.llm.exception.LlmExternalApiException;
import com.tbank.aihelper.llm.exception.ParsingResponseException;
import com.tbank.aihelper.llm.config.OpenRouterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenRouterGrokAdapter implements LLMAdapter {

    private final RestTemplate restTemplate;
    private final OpenRouterProperties properties;

    @Override
    public ChatCompletionResponse chatCompletion(ChatCompletionRequest request) {
        String model = request.getModel() != null ? request.getModel() : properties.getModel();
        List<ChatMessage> messages = request.getMessages();

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        if (request.getTemperature() != null) body.put("temperature", request.getTemperature());
        if (request.getMaxTokens() != null) body.put("max_tokens", request.getMaxTokens());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + properties.getApiKey());
        headers.set("HTTP-Referer", properties.getReferer());
        headers.set("X-Title", properties.getTitle());

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
            // Преобразуем в ChatCompletionResponse
            return restTemplate.getMessageConverters().stream()
                    .findFirst()
                    .map(c -> new com.fasterxml.jackson.databind.ObjectMapper().convertValue(responseBody, ChatCompletionResponse.class))
                    .orElseThrow(() -> new ParsingResponseException(new RuntimeException("Не удалось преобразовать ответ")));
        } catch (Exception e) {
            throw new ParsingResponseException(e);
        }
    }
}