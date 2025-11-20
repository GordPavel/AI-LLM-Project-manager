package com.tbank.aihelper.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionDefinition {
    private String name;
    private String description;

    @JsonProperty("parameters")
    private Parameters parameters;
}
