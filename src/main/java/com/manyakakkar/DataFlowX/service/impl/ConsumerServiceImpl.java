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
    public void processData(UserDto userDto) {
        try {
            if (userDto == null || userDto.getEmailId() == null) {
                return;
            }
            if (userRepository.existsByEmailId(userDto.getEmailId())) {
                return;
            }


            User user = new User();
            user.setName(userDto.getName());
            user.setEmailId(userDto.getEmailId());
            user.setMobile(userDto.getMobile());

            userRepository.save(user);
        } catch (Exception e) {
            System.err.println("Failed to process user: " + userDto + "Error: " + e.getMessage());
        }
    }
}