package com.tbank.aihelper.telegrambot.exception;

public class SchedulerException extends BaseTelegramBotException {
    
    public SchedulerException(String messageToLog, String messageToUser, Long chatId) {
        super(messageToLog, messageToUser, chatId);
    }

    public SchedulerException(String messageToLog, String messageToUser, Throwable cause, Long chatId) {
        super(messageToLog, messageToUser, chatId, cause);    
    }
}
