package com.tbank.aihelper.telegrambot.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tbank.aihelper.telegrambot.entity.JobBindingChat;
import com.tbank.aihelper.telegrambot.repository.JobBindingChatRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JobBindingService {
    private final JobBindingChatRepository jobBindingChatRepository;

    @Transactional
    public void cleanupJobBinding(String jobId) {
        jobBindingChatRepository.deleteByJobId(jobId);
    }

    public JobBindingChat save(JobBindingChat jobBindingChat) {
        return jobBindingChatRepository.save(jobBindingChat);
    }

    public Optional<JobBindingChat> getByJobId(String jobId) {
        return jobBindingChatRepository.findByJobId(jobId.replace("#", ""));
    }

}
