package com.tbank.aihelper.telegrambot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", unique = true)
    private Long chatId;

    @Column(name = "configuration")
    private String configuration;

    @Column(name = "created_at", updatable = false)
    private java.time.ZonedDateTime createdAt = java.time.ZonedDateTime.now();

    @Column(name = "updated_at")
    private java.time.ZonedDateTime updatedAt = java.time.ZonedDateTime.now();
}
