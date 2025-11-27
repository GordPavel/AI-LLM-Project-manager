package com.tbank.aihelper.llm.exception;

public class ParsingResponseException extends LlmExternalApiException {
    public ParsingResponseException(Throwable cause) {
        super("Не удалось распарсить ответ от LLM", cause);
    }
}
