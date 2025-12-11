package com.tbank.aihelper.llm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionRequest {
    private String model;
    private List<ChatMessage> messages = new ArrayList<>();
    private Double temperature;
    private Integer maxTokens;
    private Double topP;
    private Boolean stream;
    private List<String> stop;
    private List<Tool> tools;


    public void addMessage(ChatMessage message) {
        this.messages.add(message);
    }
}
