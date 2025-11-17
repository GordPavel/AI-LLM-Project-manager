package com.tbank.aihelper.knowledgebase.bookstack.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bookstack.api")
public class BookStackProperties {

    private String url = "https://bookstack.bassopaolo.com/api";
    private String token;
}
