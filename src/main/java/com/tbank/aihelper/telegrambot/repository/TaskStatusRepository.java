package com.tbank.aihelper.telegrambot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tbank.aihelper.telegrambot.entity.TaskStatus;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {
    Optional<TaskStatus> findByStatus(TaskStatus.Status status);
}
