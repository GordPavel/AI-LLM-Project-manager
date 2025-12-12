package com.tbank.aihelper.telegrambot.exception;

public class EmptyMessageException extends BaseTelegramBotException {
    
    public EmptyMessageException(String command, Long chatId) {
        super(
            "Command: '" + command + "' received incomplete/empty input.", 
            "Недостаточно данных для выполнения команды. Пожалуйста, укажите все необходимые параметры.",
            chatId
        );
    }

    public EmptyMessageException(String command, Throwable cause, Long chatId) {
        super(
            "Command: '" + command + "' received incomplete/empty input.", 
            "Недостаточно данных для выполнения команды. Пожалуйста, укажите все необходимые параметры.",
            chatId,
            cause
        );    
    }
}
