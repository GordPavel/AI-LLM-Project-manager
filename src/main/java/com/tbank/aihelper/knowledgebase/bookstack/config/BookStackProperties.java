package com.tbank.aihelper.knowledgebase.bookstack.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bookstack.api")
public class BookStackProperties {

    @NotBlank
    private String url = "https://bookstack.bassopaolo.com/api";

    @NotBlank(message = "BookStack Token ID обязателен")
    private String tokenId;

    @NotBlank(message = "BookStack Token Secret обязателен")
    private String tokenSecret;

    // Удобный метод для заголовков (часто используется)
    public String getBearerToken() {
        return "Token " + tokenId + ":" + tokenSecret;
    }
}
