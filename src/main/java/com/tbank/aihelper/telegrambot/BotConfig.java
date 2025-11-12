package com.tbank.aihelper.telegrambot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pengrad.telegrambot.TelegramBot;

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
    public ChatAdapter chatAdapter(TelegramBot bot) {
        return new ChatBotImpl(bot);
    }
}
