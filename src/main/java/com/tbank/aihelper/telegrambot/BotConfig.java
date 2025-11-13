package com.tbank.aihelper.telegrambot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pengrad.telegrambot.TelegramBot;
import com.tbank.aihelper.telegrambot.observer.ObserverChatBotAdapter;

@Configuration
public class BotConfig {

    @Bean
    public TelegramBot telegramBot(@Value("${telegram.bot.token}") String botToken) {
        if (botToken == null || botToken.trim().isEmpty()) {
            throw new IllegalStateException("Telegram bot token is missing");
        }
        return new TelegramBot(botToken);
    }
    
    @Bean(initMethod = "start", destroyMethod = "stop")
    public ChatBotAdapter chatAdapter(TelegramBot bot, ObserverChatBotAdapter observerChatBotAdapter) {
        return new ChatBotImpl(bot, observerChatBotAdapter);
    }
}
