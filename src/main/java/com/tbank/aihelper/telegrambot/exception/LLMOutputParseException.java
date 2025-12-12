package com.tbank.aihelper.telegrambot.exception;

public class LLMOutputParseException extends BaseTelegramBotException {
    
    public LLMOutputParseException(
        String outputMessageLLM, 
        String command,
        Long chatId
    ) {
        super(
            "Failed to parse LLM response for command: '" + command + "'; message failed to parse: '" + outputMessageLLM + "'.", 
            "Не удалось обработать ваш запрос. Пожалуйста, переформулируйте команду или уточните детали.",
            chatId
        );
    }

    public LLMOutputParseException(
        String outputMessageLLM, 
        String command,
        Long chatId, 
        Throwable cause
    ) {
        super(
            "Failed to parse LLM response for command: '" + command + "'; message failed to parse: " + outputMessageLLM + "'.", 
            "Не удалось обработать ваш запрос. Пожалуйста, переформулируйте команду или уточните детали.",
            chatId, 
            cause
        );
    }
}
