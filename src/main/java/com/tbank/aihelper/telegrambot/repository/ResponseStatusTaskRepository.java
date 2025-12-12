package com.tbank.aihelper.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tbank.aihelper.telegrambot.entity.ResponseStatusTask;

public interface ResponseStatusTaskRepository extends JpaRepository<ResponseStatusTask, Long> {
    
}
