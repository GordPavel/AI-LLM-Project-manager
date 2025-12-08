package com.tbank.aihelper.telegrambot.exception;

public class NotFoundException extends BaseTelegramBotException {
    
    public NotFoundException(String object, String idObject, String typeLocation) {
        super("Not found '" + object + "' with id: " + idObject + " in '" + typeLocation + "'");
    }

    public NotFoundException(String object, String idObject, String typeLocation, Throwable cause) {
        super("Not found '" + object + "' with id: " + idObject + " in '" + typeLocation + "'", cause);    
    }
}
