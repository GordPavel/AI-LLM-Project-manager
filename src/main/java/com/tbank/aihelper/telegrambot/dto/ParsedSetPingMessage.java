package com.tbank.aihelper.telegrambot.dto;

import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParsedSetPingMessage {
    private SetPingDto setPingDto;
    private ZonedDateTime zonedDateTime;
}
