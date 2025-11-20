package com.tbank.aihelper.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Choice {
    private int index;
    private ChatMessage message;

    @JsonProperty("finish_reason")
    private String finishReason;
}
