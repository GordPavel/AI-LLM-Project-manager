package com.tbank.aihelper.telegrambot.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tbank.aihelper.telegrambot.entity.JobBindingChat;
import com.tbank.aihelper.telegrambot.exception.NotFoundException;
import com.tbank.aihelper.telegrambot.repository.JobBindingChatRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class JobBindingService {
    private final JobBindingChatRepository jobBindingChatRepository;

    @Transactional
    public void deleteJobById(Long id) {
        jobBindingChatRepository.deleteById(id);
    }

    public JobBindingChat save(JobBindingChat jobBindingChat) {
        return jobBindingChatRepository.save(jobBindingChat);
    }

    public Optional<JobBindingChat> getByChatId(String taskId) {
        return jobBindingChatRepository.findByTaskId(taskId);
    }

    public JobBindingChat getByJobId(String jobIdStr, Long chatId) throws NotFoundException {
        Long jobId = Long.valueOf(
            Optional.ofNullable(jobIdStr)
                .filter(s -> s.startsWith("#"))
                .map(s -> s.substring(1))
                .orElse(jobIdStr)
        );
        return getByJobId(jobId, chatId);
    }

    public JobBindingChat getByJobId(Long jobId, Long chatId) throws NotFoundException {
        return jobBindingChatRepository.findById(jobId)
            .orElseThrow(() -> new NotFoundException(
                    "Not found 'JobBindingChat' with id: '" + jobId + "' in database.",
                    "Не удалось найти пинг с идентификатором: #" + jobId + ".",
                    chatId
                ));
    }

}
