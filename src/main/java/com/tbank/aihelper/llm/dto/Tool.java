package com.tbank.aihelper.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tool {
    private String type = "function";
    private FunctionDefinition function;

    public Tool(FunctionDefinition function) {
        this.function = function;
    }
}
