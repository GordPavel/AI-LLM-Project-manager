package com.tbank.aihelper.telegrambot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tbank.aihelper.telegrambot.observer.ObserverChatBotAdapter;
import com.tbank.aihelper.telegrambot.observer.ObserverChatBotImpl;

@Configuration
public class ObserverConfig {
    
    @Bean
    public ObserverChatBotAdapter observerAdapter() {
        return new ObserverChatBotImpl();
    }
}
