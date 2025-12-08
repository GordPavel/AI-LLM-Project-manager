package com.tbank.aihelper.telegrambot.exception;

public class BaseTelegramBotException extends RuntimeException {
    
    public BaseTelegramBotException(String message) {
        super(message);
    }

    public BaseTelegramBotException(String message, Throwable cause) {
        super(message, cause);
    }
}
