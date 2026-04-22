package com.chatapp.service;

import com.chatapp.dto.user.UserResponse;
import com.chatapp.exception.ResourceNotFoundException;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PresenceService presenceService;

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    @Transactional
    public void updatePublicKey(UUID userId, String publicKey) {
        User user = getUserById(userId);
        user.setPublicKey(publicKey);
        userRepository.save(user);
        log.info("Public key updated for user: {}", userId);
    }

    public String getPublicKey(UUID userId) {
        User user = getUserById(userId);
        return user.getPublicKey();
    }

    public java.util.List<User> searchByUsername(String query) {
        return userRepository.findByUsernameContainingIgnoreCase(query);
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .publicKey(user.getPublicKey())
                .createdAt(user.getCreatedAt())
                .online(true)
                .build();
    }
}
