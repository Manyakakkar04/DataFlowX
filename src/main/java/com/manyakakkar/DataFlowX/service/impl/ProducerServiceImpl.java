package com.manyakakkar.DataFlowX.service.impl;

import com.manyakakkar.DataFlowX.dto.UploadMessage;
import com.manyakakkar.DataFlowX.dto.UserDto;
import com.manyakakkar.DataFlowX.dto.ValidateRequestContext;
import com.manyakakkar.DataFlowX.entity.Upload;
import com.manyakakkar.DataFlowX.repository.UploadRepository;
import com.manyakakkar.DataFlowX.security.CustomUserDetails;
import com.manyakakkar.DataFlowX.service.IValidators;
import com.manyakakkar.DataFlowX.service.ProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProducerService {

    private final KafkaTemplate<String, UploadMessage> kafkaTemplate;

    private final List<IValidators> validatorsList;
    private final UploadRepository uploadRepository;

    private static final String TOPIC = "bulk-upload-topic";

    @Override
    public String publishToKafka(MultipartFile file) {

    log.info("validating the request for user : {}",getCurrentUserEmail());
        ValidateRequestContext context = new ValidateRequestContext(file,getCurrentUserId());

        validatorsList.forEach(v -> v.validate(context));
        log.info("validation done");
        Long rowNumber=1L;
        int count = 0;
        log.info("Sending to kafka queue");
        Long userId = getCurrentUserId();

// TODO: save in DB (Upload table)
        Long uploadId = createUploadEntry(userId, file.getOriginalFilename());
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            // skip header
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 3) continue;

                UserDto userDto = new UserDto();
                userDto.setName(parts[0].trim());
                userDto.setEmailId(parts[1].trim());

                try {
                    userDto.setMobile(Long.parseLong(parts[2].trim()));
                } catch (NumberFormatException e) {
                    continue;
                }
                UploadMessage message = new UploadMessage(
                        uploadId,
                        rowNumber,
                        userDto
                );


                kafkaTemplate.send(TOPIC, message);
                log.info("sent row no.: {}", rowNumber);
                count++;
                rowNumber = rowNumber+1;

            }

        } catch (Exception e) {
            log.error("Error processing file for user {}", getCurrentUserEmail(), e);
            return "Error processing file";
        }

        return "Successfully published " + count + " records";
    }

    private Long createUploadEntry(Long userId, String fileName) {

        Upload upload = new Upload();
        upload.setUserId(userId);
        upload.setFileName(fileName);
        upload.setStatus("PROCESSING");


        Upload saved = uploadRepository.save(upload);

        return saved.getUploadId(); // this is uploadId
    }

    private String getCurrentUserEmail() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {

            return "anonymous user";
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUsername(); // email
        }

        return principal.toString();
    }


    private Long getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }

        throw new RuntimeException("Invalid principal");
    }
}
