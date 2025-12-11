package com.tbank.aihelper.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyParameters;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import com.tbank.aihelper.telegrambot.dto.BotMessage;
import com.tbank.aihelper.telegrambot.dto.UpdateContext;
import com.tbank.aihelper.telegrambot.exception.BaseTelegramBotException;
import com.tbank.aihelper.telegrambot.exception.LLMOutputParseException;
import com.tbank.aihelper.telegrambot.observer.ObserverChatBotAdapter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatBotImpl implements ChatBotAdapter {
    private final ObserverChatBotAdapter observerChatBotAdapter;
    private final TelegramBot bot;
    
    public ChatBotImpl(TelegramBot bot, ObserverChatBotAdapter observerChatBotAdapter) {
        this.observerChatBotAdapter = observerChatBotAdapter;
        this.bot = bot;
    }

    public void start() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                try {
                    processUpdate(update);
                } catch (LLMOutputParseException e) {
                    log.error("{}", e.getMessage());
                    sendMessage(BotMessage.builder()
                            .chatId(e.getChatId())
                            .textMessage(e.getMessageToClient())
                        .build()
                    );
                } catch (BaseTelegramBotException e) {
                    log.warn("{}", e.getMessage());
                    sendMessage(BotMessage.builder()
                            .chatId(e.getChatId())
                            .textMessage(e.getMessageToClient())
                        .build()
                    );
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
                .messageId(message.messageId())
                .userId(message.from().id())
                .userName(message.from().username())
                .isGroupChat(isGroupChat(message))
            .build();

        parseMessageAndSet(message.text(), updateContext);
        
        log.debug("Received message: {}", updateContext.getTextMessage());
        handleNewMessage(updateContext);
    }

    private void parseMessageAndSet(String message, UpdateContext updateContext) {
        if(!message.startsWith("/")) {
            updateContext.setTextMessage(message);
            return;
        }

        int spaceIndex = message.indexOf(' ');
        if(spaceIndex < 0) {
            updateContext.setCommand(message);
            return;
        }

        updateContext.setCommand(message.substring(0, spaceIndex));
        updateContext.setTextMessage(message.substring(spaceIndex + 1));
    }

    public void sendMessage(BotMessage message) {
        SendMessage request = new SendMessage(message.getChatId().longValue(), message.getTextMessage());
        
        if (message.getReplyToMessageId() != null) {
            request.replyParameters(new ReplyParameters(message.getReplyToMessageId()));
        }

        SendResponse response = bot.execute(request);
        if (!response.isOk()) {
            log.error("Failed to send message: {}", response.description());
        }
    }

    public void handleNewMessage(UpdateContext updateContext) {
        observerChatBotAdapter.notifyNewMessage(updateContext);
    }

    private boolean isGroupChat(Message message) {
        return !message.chat().type().equals("private");
    }
    
    public void stop() {
        bot.removeGetUpdatesListener();
        log.info("Bot stopped");    
    }
}
