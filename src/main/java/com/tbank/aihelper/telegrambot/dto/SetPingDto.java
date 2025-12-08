package com.tbank.aihelper.telegrambot.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetPingDto implements Serializable {
    private Long chatId;
    private String taskId;
    private Integer messageId;
    private List<String> usernamesToPing;
}
