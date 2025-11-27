package com.tbank.aihelper.llm.exception;

public class EmptyResponseException extends LlmExternalApiException {
    public EmptyResponseException() {
        super("LLM вернула пустой ответ (body == null)");
    }
}
