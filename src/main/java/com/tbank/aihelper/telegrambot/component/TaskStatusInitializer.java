package com.tbank.aihelper.telegrambot.component;

import org.springframework.stereotype.Component;

import com.tbank.aihelper.telegrambot.entity.TaskStatus;
import com.tbank.aihelper.telegrambot.repository.TaskStatusRepository;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class TaskStatusInitializer {
    
    private final TaskStatusRepository repository;

    @PostConstruct
    public void init() {
        if (repository.count() == 0) {
            for (TaskStatus.Status status : TaskStatus.Status.values()) {
                TaskStatus ts = new TaskStatus();
                ts.setStatus(status);
                repository.save(ts);
            }
        }
    }
}
