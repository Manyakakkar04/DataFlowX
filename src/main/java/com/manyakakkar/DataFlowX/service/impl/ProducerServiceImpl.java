package com.manyakakkar.DataFlowX.service.impl;

import com.manyakakkar.DataFlowX.dto.UserDto;
import com.manyakakkar.DataFlowX.service.ProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@RequiredArgsConstructor
@Service
public class ProducerServiceImpl implements ProducerService {

    private final KafkaTemplate<String, UserDto> kafkaTemplate;

    private static final String TOPIC = "bulk-upload-topic";

//    public ProducerServiceImpl(KafkaTemplate<String, String> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }

    @Override
    public String publishToKafka(MultipartFile file) {

        int count = 0;

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            // Skip header
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");

                if (parts.length < 3) {
                    continue;
                }

                // ✅ CREATE DTO PER ROW
                UserDto userDto = new UserDto();
                userDto.setName(parts[0].trim());
                userDto.setEmailId(parts[1].trim());
                try {
                    userDto.setMobile(Long.parseLong(parts[2].trim()));
                } catch (NumberFormatException e) {
                    continue;
                }

                kafkaTemplate.send(TOPIC, userDto);

                count++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing file";
        }

        return "Successfully processed " + count + " records";
    }
}