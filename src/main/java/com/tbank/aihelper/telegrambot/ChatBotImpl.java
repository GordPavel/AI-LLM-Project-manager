package com.tbank.aihelper.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import com.tbank.aihelper.telegrambot.dto.BotMessage;
import com.tbank.aihelper.telegrambot.dto.UpdateContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatBotImpl implements ChatBotAdapter {
    private final TelegramBot bot;
    
    public ChatBotImpl(TelegramBot bot) {
        this.bot = bot;
    }

    public void start() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                try {
                    processUpdate(update);
                } catch (Exception e) {
                    log.error("Error processing update: {}", e.getMessage());                
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        
        log.info("Bot started successfully!");
    }

    private void processUpdate(Update update) {
        Message message = update.message();
        if (message == null || message.text() == null) return;
        
        UpdateContext updateContext = UpdateContext.builder()
                .chatId(message.chat().id())
                .text(message.text())
                .messageId(message.messageId())
                .userId(message.from().id())
                .userName(message.from().username())
                .isGroupChat(isGroupChat(message))
            .build();
        
        log.debug("Received message: {}", updateContext.getText());
        if (updateContext.getText().startsWith("/")) {
            handleCommand(updateContext);
        } else if(updateContext.isGroupChat()) {
            handleTextMessage(updateContext);
        }
    }

    public void sendMessage(BotMessage message) {
        SendMessage request = new SendMessage(message.getChatId(), message.getTextMessage());
        
        if (message.getReplyToMessageId() != null) {
            request.replyToMessageId(message.getReplyToMessageId().intValue());
        }
        
        SendResponse response = bot.execute(request);
        
        if (!response.isOk()) {
            log.error("Failed to send message: {}", response.description());
        }
    }

    public void handleCommand(UpdateContext updateContext) {
        BotMessage botAnswer = BotMessage.builder()
                .chatId(updateContext.getChatId())
                .replyToMessageId(updateContext.getMessageId())
            .build();
        
        switch (updateContext.getText().toLowerCase()) {
            case "/start":
                botAnswer.setTextMessage("Добавь меня в групповую беседу для прослушивания.");
                break;
            case "/help":
                botAnswer.setTextMessage("Доступные команды:\n/start - начать\n/help - помощь.");
                break;
            default:
                botAnswer.setTextMessage("Неизвестная команда. Используйте /help для списка команд.");
                break;
        }
        
        sendMessage(botAnswer);
    }

    public void handleTextMessage(UpdateContext updateContext) {
        // Или просто вызвать сервисный метод для уведомления о пришедшем сообщении, или через Обсерв.
    }

    private boolean isGroupChat(Message message) {
        return !message.chat().type().equals("private");
    }
    
    public void stop() {
        bot.removeGetUpdatesListener();
        log.info("Bot stopped");    
    }
}
