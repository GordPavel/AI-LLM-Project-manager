package com.tbank.aihelper.llm.exception;

import org.springframework.http.HttpStatus;

public class ExternalApiException extends BaseException {
    public ExternalApiException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_GATEWAY);
    }
}
