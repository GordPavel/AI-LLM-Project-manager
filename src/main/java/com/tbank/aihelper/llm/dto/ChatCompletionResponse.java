package com.tbank.aihelper.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatCompletionResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    public String getFirstMessageContent() {
        if (choices == null || choices.isEmpty()) return null;
        ChatMessage msg = choices.get(0).getMessage();
        return msg != null ? msg.getContent() : null;
    }
}
