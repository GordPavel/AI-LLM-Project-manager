package com.tbank.aihelper.telegrambot.observer;

import com.tbank.aihelper.telegrambot.dto.UpdateContext;

public interface EventListenerChatBot {
    void handleNewMessage(UpdateContext message);
}
