package com.manyakakkar.DataFlowX.service.impl;

import com.manyakakkar.DataFlowX.dto.UserDto;
import com.manyakakkar.DataFlowX.entity.User;
import com.manyakakkar.DataFlowX.repository.UserRepository;
import com.manyakakkar.DataFlowX.service.ConsumerService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ConsumerServiceImpl implements ConsumerService {
    private final UserRepository userRepository;
    @KafkaListener(
            topics = "bulk-upload-topic",
            groupId = "bulk-group"
    )
    @Override
    public void processData(String message) {
        try {
            if (message == null || message.trim().isEmpty()) {
                return;
            }
            String[] parts = message.split(",");
            if (parts.length < 3) {
                return;
            }
            UserDto user = new UserDto();
            user.setName(parts[0].trim());
            user.setEmailId(parts[1].trim());

            String mobileStr = parts[2].trim();
            try {
                user.setMobile(Long.parseLong(mobileStr));
            } catch (NumberFormatException e) {
                return;
            }

            User newUser = new User();
            newUser.setName(user.getName());
            newUser.setEmailId(user.getEmailId());
            newUser.setMobile(user.getMobile());

            userRepository.save(newUser);
        } catch (Exception e) {
            System.err.println("Failed to process message: " + message + "Error: " + e.getMessage());
        }
    }
}