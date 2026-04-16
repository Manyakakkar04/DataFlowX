package com.manyakakkar.DataFlowX.service.impl;

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
            String[] parts = message.split(",");
            if (parts.length < 3) {
                System.err.println("Skipping malformed row: " + message);
                return;
            }
            User user = new User();
            user.setName(parts[0].trim());
            user.setEmailId(parts[1].trim());
            user.setMobile(Long.valueOf(parts[2].trim()));
            userRepository.save(user);
        } catch (Exception e) {
            System.err.println("Failed to process message: " + message + " | Error: " + e.getMessage());
        }
    }
}
