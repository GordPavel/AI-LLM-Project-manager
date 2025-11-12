package com.tbank.aihelper.telegrambot;

import com.tbank.aihelper.telegrambot.dto.BotMessage;
import com.tbank.aihelper.telegrambot.dto.UpdateContext;

public interface ChatBotAdapter {
    void sendMessage(BotMessage message);
    void handleCommand(UpdateContext updateContext);
    void handleTextMessage(UpdateContext updateContext);
}
