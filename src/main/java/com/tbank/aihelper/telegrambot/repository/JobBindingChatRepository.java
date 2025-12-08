package com.tbank.aihelper.telegrambot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tbank.aihelper.telegrambot.entity.JobBindingChat;

public interface JobBindingChatRepository extends JpaRepository<JobBindingChat, Long> {
    Optional<JobBindingChat> findByJobId(String jobId);
    void deleteByJobId(String jobId);
}
