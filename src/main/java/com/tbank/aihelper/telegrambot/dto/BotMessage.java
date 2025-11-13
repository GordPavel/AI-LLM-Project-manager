package com.tbank.aihelper.telegrambot.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BotMessage {
    private Long chatId;
    private Integer replyToMessageId;
    private String textMessage;
}
