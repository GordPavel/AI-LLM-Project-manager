package com.tbank.aihelper.llm.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "llm.openrouter")
public class OpenRouterProperties {

    @NotBlank
    private String apiKey;

    private String model = "amazon/nova-2-lite-v1:free";

    @NotBlank
    private String baseUrl = "https://openrouter.ai/api/v1";

    private String referer = "http://localhost:8080";
    
    private String title = "AI Project Manager";
}
