package com.tbank.aihelper.llm.exception;

public class ToolExecutionException extends LlmExternalApiException {
    public ToolExecutionException(String toolName, Throwable cause) {
        super("Ошибка при выполнении инструмента '" + toolName + "'", cause);
    }
}
