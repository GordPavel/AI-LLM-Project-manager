package com.tbank.aihelper.telegrambot.service;

import org.springframework.stereotype.Service;

import com.tbank.aihelper.telegrambot.ChatBotAdapter;
import com.tbank.aihelper.telegrambot.dto.BotMessage;
import com.tbank.aihelper.telegrambot.dto.UpdateContext;
import com.tbank.aihelper.telegrambot.exception.EmptyMessageException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CommandUtilsService {

    private final ChatBotAdapter chatBotAdapter;
    
    public boolean shouldHandle(UpdateContext ctx, String expectedCommand) {
        return ctx.getCommand() != null && ctx.getCommand().equals(expectedCommand);
    }

    public void ensureNonEmptyMessage(UpdateContext ctx, String command) {
        if(ctx.getTextMessage() == null || ctx.getTextMessage().isEmpty())
            throw new EmptyMessageException(command, ctx.getChatId());
    }

    public void fastSend(UpdateContext ctx, String message) {
        chatBotAdapter.sendMessage(BotMessage.builder()
                .chatId(ctx.getChatId())
                .replyToMessageId(ctx.getMessageId())
                .textMessage(message)
            .build());
    }
}
