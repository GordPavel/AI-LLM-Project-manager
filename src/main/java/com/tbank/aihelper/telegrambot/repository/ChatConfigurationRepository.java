package com.tbank.aihelper.telegrambot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tbank.aihelper.telegrambot.entity.ChatConfiguration;

public interface ChatConfigurationRepository extends JpaRepository<ChatConfiguration, Long> {
    Optional<ChatConfiguration> findByChatId(Long chatId);
}
