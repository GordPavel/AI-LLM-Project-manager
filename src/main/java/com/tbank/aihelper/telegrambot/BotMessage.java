package com.tbank.aihelper.telegrambot;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BotMessage {
    private Long chatId;
    private Long replyToMessageId;
    private String textMessage;
}
