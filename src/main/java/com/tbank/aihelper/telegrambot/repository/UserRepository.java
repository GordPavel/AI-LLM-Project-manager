package com.tbank.aihelper.telegrambot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tbank.aihelper.telegrambot.entity.TgUser;

public interface UserRepository extends JpaRepository<TgUser, Long> {
    Optional<TgUser> findByUsername(String username);
}
