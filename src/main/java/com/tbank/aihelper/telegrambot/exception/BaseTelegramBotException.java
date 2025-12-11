package com.tbank.aihelper.telegrambot.exception;

import lombok.Getter;

@Getter
public abstract class BaseTelegramBotException extends RuntimeException {
    private final Long chatId;
    private final String messageToClient;

    public BaseTelegramBotException(String message, String messageToClient, Long chatId) {
        super(message);
        this.chatId = chatId;
        this.messageToClient = messageToClient;
    }

    public BaseTelegramBotException(String message, String messageToClient, Long chatId, Throwable cause) {
        super(message, cause);
        this.chatId = chatId;
        this.messageToClient = messageToClient;
    }
}
