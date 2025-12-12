package com.tbank.aihelper.telegrambot.exception;

public class LLMDataDeficitException extends BaseTelegramBotException {
    
    public LLMDataDeficitException(
        String messageDeficitInfo, 
        String command,
        Long chatId
    ) {
        super(
            "Insufficient input for command: '" + command + "''; message: '" + messageDeficitInfo + "'.", // Как идея отображать параметры которых не хватило 
            "Недостаточно информации для выполнения действия. Пожалуйста, уточните детали запроса.",
            chatId
        );
    }

    public LLMDataDeficitException(
        String messageDeficitInfo, 
        String command,
        Long chatId, 
        Throwable cause
    ) {
        super(
            "Insufficient input for command: '" + command + "'; message: " + messageDeficitInfo + "'.",
            "Недостаточно информации для выполнения действия. Пожалуйста, уточните детали запроса.",
            chatId, 
            cause
        );
    }
}
