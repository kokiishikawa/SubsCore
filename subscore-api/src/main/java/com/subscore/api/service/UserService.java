package com.subscore.api.service;

import com.subscore.api.model.User;
import com.subscore.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public User registerUser(User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isPresent()) {
            // 既存ユーザーの更新
            User updateUser = existingUser.get();
            updateUser.setName(user.getName());
            updateUser.setImage(user.getImage());
            updateUser.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(updateUser);
        }

        // 新規ユーザーの作成
        user.setId(UUID.randomUUID());
        user.setEmailVerified(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public UUID getUserIdByEmail(String email) {
        System.out.println("Email: " + email);
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("ユーザーIDが見つかりません。"));
    }
}
