package com.tbank.aihelper.telegrambot.observer;

import java.util.ArrayList;
import java.util.List;

import com.tbank.aihelper.telegrambot.dto.UpdateContext;

public class ObserverChatBotImpl implements ObserverChatBotAdapter {
    private List<EventListenerChatBot> listeners = new ArrayList<>();

    public void subscribe(EventListenerChatBot listener) {
        this.listeners.add(listener);
    }

    public void unsubscribe(EventListenerChatBot listener) {
        this.listeners.remove(listener);
    }

    public void notifyNewMessage(UpdateContext message) {
        this.listeners.forEach(listener -> listener.handleNewMessage(message));
    }
}
