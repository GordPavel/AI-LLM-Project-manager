package com.tbank.aihelper.telegrambot.service;

import org.springframework.stereotype.Service;

import com.tbank.aihelper.telegrambot.entity.TgUser;
import com.tbank.aihelper.telegrambot.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public TgUser getOrCreateUser(String username) {
        return userRepository.findByUsername(username)
            .orElseGet(() -> userRepository.save(TgUser.builder().username(username).build()));
    }
    
}
