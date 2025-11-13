package com.tbank.aihelper.telegrambot.observer;

import com.tbank.aihelper.telegrambot.dto.UpdateContext;

public interface ObserverChatBotAdapter {
    void subscribe(EventListenerChatBot listener);
    void unsubscribe(EventListenerChatBot listener);
    void notifyNewMessage(UpdateContext message);
}
