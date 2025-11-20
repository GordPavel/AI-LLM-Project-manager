package com.tbank.aihelper.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String role;
    private String content;
    private String name;
    private List<ToolCall> toolCalls;
    @JsonProperty("tool_call_id")
    private String toolCallId;

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
