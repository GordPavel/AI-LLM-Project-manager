package com.tbank.aihelper.llm.exception;

public class LlmExternalApiException extends ExternalApiException {
    public LlmExternalApiException(String message) {
        super("[LLM API] " + message);
    }

    public LlmExternalApiException(String message, Throwable cause) {
        super("[LLM API] " + message, cause);
    }
}
