package com.tbank.aihelper.telegrambot.entity;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConfiguration {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "configuration", nullable = false)
    private String configuration;

    @Column(name = "created_at", updatable = false)
    private java.time.ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
