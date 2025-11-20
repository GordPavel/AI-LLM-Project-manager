package com.tbank.aihelper.telegrambot.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateContext {
    private Long chatId;
    private String textMessage;
    private String command;
    private Integer messageId;
    private Long userId;
    private String userName;
    private boolean isGroupChat;
}
