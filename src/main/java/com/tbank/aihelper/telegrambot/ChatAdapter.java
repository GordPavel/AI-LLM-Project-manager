package com.tbank.aihelper.telegrambot;

import com.tbank.aihelper.telegrambot.dto.BotMessage;

public interface ChatAdapter {
    void sendMessage(BotMessage message);
}
