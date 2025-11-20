package com.tbank.aihelper.repository;

import com.tbank.aihelper.domain.TaskBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TaskBindingRepository extends JpaRepository<TaskBinding, Long> {
    Optional<TaskBinding> findByTrelloCardId(String trelloCardId);
    Optional<TaskBinding> findByTelegramChatId(Long telegramChatId);
    boolean existsByTrelloCardId(String trelloCardId);
    void deleteByTrelloCardId(String trelloCardId);
}