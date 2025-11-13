package com.tbank.aihelper.TaskTracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "trello")
public class TrelloConfig {
    private String apiKey;
    private String token;
    private String boardId;
}