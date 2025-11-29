package com.tbank.aihelper.llm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolResponse {
    private String type;   // "task", "comment", "error"
    private Object data;
    private String error;

    public static ToolResponse task(Object task) {
        return new ToolResponse("task", task, null);
    }

    public static ToolResponse comment(Object comment) {
        return new ToolResponse("comment", comment, null);
    }

    public static ToolResponse error(String message) {
        return new ToolResponse("error", null, message);
    }
}
