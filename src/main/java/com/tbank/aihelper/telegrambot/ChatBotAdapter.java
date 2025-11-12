package com.tbank.aihelper.telegrambot;

import com.tbank.aihelper.telegrambot.dto.BotMessage;

public interface ChatBotAdapter {
    void sendMessage(BotMessage message);
}
