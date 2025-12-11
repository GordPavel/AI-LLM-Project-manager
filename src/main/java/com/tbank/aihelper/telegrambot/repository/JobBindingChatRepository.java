package com.tbank.aihelper.telegrambot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tbank.aihelper.telegrambot.entity.JobBindingChat;
import com.tbank.aihelper.telegrambot.entity.TgUser;

public interface JobBindingChatRepository extends JpaRepository<JobBindingChat, Long> {
    Optional<JobBindingChat> findByTaskId(String chatId);
    List<JobBindingChat> findByTaskIdAndPerformersContaining(String taskId, TgUser performer);
}
