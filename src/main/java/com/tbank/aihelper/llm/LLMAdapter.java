package com.tbank.aihelper.llm;

import com.tbank.aihelper.llm.dto.ChatCompletionRequest;
import com.tbank.aihelper.llm.dto.ChatCompletionResponse;
import com.tbank.aihelper.llm.dto.ChatMessage;
import com.tbank.aihelper.llm.dto.Tool;

import java.util.List;

public interface LLMAdapter {

    ChatCompletionResponse chatCompletion(ChatCompletionRequest request);

    default String ask(String prompt) {
        return ask(prompt, null, null, null);
    }

    default String ask(String prompt, String model) {
        return ask(prompt, model, null, null);
    }

    default String ask(String prompt, String model, Double temperature) {
        return ask(prompt, model, temperature, null);
    }

    default String ask(String prompt, String model, Double temperature, Integer maxTokens) {
        var message = new ChatMessage("user", prompt);

        var request = ChatCompletionRequest.builder()
                .model(model)
                .messages(List.of(message))
                .temperature(temperature)
                .maxTokens(maxTokens)
                .tools(getDefaultTools())
                .build();

        ChatCompletionResponse response = chatCompletion(request);
        return response.getFirstMessageContent();
    }

    default List<Tool> getDefaultTools() {
        return List.of();
    }
}
