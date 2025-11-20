// src/main/java/com/tbank/aihelper/service/TaskBindingService.java
package com.tbank.aihelper.service;

import com.tbank.aihelper.domain.TaskBinding;
import com.tbank.aihelper.repository.TaskBindingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskBindingService {

    private final TaskBindingRepository repository;

    public TaskBinding bindTask(String trelloCardId, Long telegramChatId) {
        TaskBinding binding = TaskBinding.builder()
                .trelloCardId(trelloCardId)
                .telegramChatId(telegramChatId)
                .build();
        return repository.save(binding);
    }

    public Optional<Long> getChatIdByCard(String trelloCardId) {
        return repository.findByTrelloCardId(trelloCardId)
                .map(TaskBinding::getTelegramChatId);
    }

    public Optional<String> getCardIdByChat(Long telegramChatId) {
        return repository.findByTelegramChatId(telegramChatId)
                .map(TaskBinding::getTrelloCardId);
    }

    public boolean isBound(String trelloCardId) {
        return repository.existsByTrelloCardId(trelloCardId);
    }

    public void unbind(String trelloCardId) {
        repository.deleteByTrelloCardId(trelloCardId);
    }
}