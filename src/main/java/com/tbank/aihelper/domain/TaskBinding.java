package com.tbank.aihelper.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_binding")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskBinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trello_card_id", nullable = false, unique = true)
    private String trelloCardId;

    @Column(name = "telegram_chat_id", nullable = false)
    private Long telegramChatId;

    @Column(name = "created_at", updatable = false)
    private java.time.ZonedDateTime createdAt = java.time.ZonedDateTime.now();

    @Column(name = "updated_at")
    private java.time.ZonedDateTime updatedAt = java.time.ZonedDateTime.now();
}