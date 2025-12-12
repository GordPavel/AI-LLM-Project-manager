package com.tbank.aihelper.telegrambot.exception;

public class NotFoundException extends BaseTelegramBotException {
    
    public NotFoundException(
        String messageToLog, 
        String messageToUser,
        Long chatId
    ) {
        super(messageToLog, messageToUser, chatId);
    }

    public NotFoundException(
        String messageToLog, 
        String messageToUser,
        Long chatId,
        Throwable cause
    ) {
        super(messageToLog, messageToUser, chatId, cause);
    }
}
