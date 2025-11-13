package com.tbank.aihelper.telegrambot;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.tbank.aihelper.telegrambot.observer.ObserverChatBotAdapter;
import com.tbank.aihelper.telegrambot.observer.ObserverChatBotImpl;

@Component
public class ObserverConfig {
    
    @Bean
    public ObserverChatBotAdapter observerAdapter() {
        return new ObserverChatBotImpl();
    }
}
