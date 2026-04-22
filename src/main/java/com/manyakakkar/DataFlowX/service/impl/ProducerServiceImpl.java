package com.manyakakkar.DataFlowX.service.impl;

import com.manyakakkar.DataFlowX.dto.UploadMessage;
import com.manyakakkar.DataFlowX.dto.UserDto;
import com.manyakakkar.DataFlowX.entity.Upload;
import com.manyakakkar.DataFlowX.tracker.UploadStatusTracker;
import com.manyakakkar.DataFlowX.repository.UploadRepository;
import com.manyakakkar.DataFlowX.security.CustomUserDetails;
import com.manyakakkar.DataFlowX.service.IValidators;
import com.manyakakkar.DataFlowX.service.ProducerService;
import com.manyakakkar.DataFlowX.dto.ValidateRequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.io.input.BOMInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProducerService {

    private final KafkaTemplate<String, UploadMessage> kafkaTemplate;
    private final List<IValidators> validatorsList;
    private final UploadRepository uploadRepository;
    private final UploadStatusTracker uploadStatusTracker;

    private static final String TOPIC = "bulk-upload-topic";

    @Override
    public String publishToKafka(MultipartFile file) {

        log.info("Validating request for user: {}", getCurrentUserEmail());
        ValidateRequestContext context = new ValidateRequestContext(file, getCurrentUserId());
        validatorsList.forEach(v -> v.validate(context));
        log.info("Validation done");

        Long userId = getCurrentUserId();
        Long uploadId = createUploadEntry(userId, file.getOriginalFilename());

        // read all valid rows first to get totalRows count ---
        List<UploadMessage> messages = new ArrayList<>();
        Long rowNumber = 1L;


        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new BOMInputStream(file.getInputStream())))) {

            // skip header
            reader.readLine();
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                log.info("Read line: '{}'", line);

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
//                if (parts.length < 3) continue;

                UserDto userDto = new UserDto();
                userDto.setName(parts[0].trim());
                userDto.setEmailId(parts[1].trim());

                String mobileStr = parts.length > 2 ? parts[2].trim() : "";
                try {
                    userDto.setMobile(Long.parseLong(mobileStr));
                } catch (NumberFormatException e) {
                    userDto.setMobile(null); // null = invalid, consumer will catch it
                }

                messages.add(new UploadMessage(uploadId, rowNumber, userDto));
                rowNumber++;
            }

        } catch (Exception e) {
            log.error("Error reading file for user {}", getCurrentUserEmail(), e);
            return "Error processing file";
        }

        long totalRows = messages.size();

        if (totalRows == 0) {
            log.warn("No valid rows found in file for uploadId={}", uploadId);
            return "No valid rows found in file";
        }

        // register before sending to Kafka ---
        uploadStatusTracker.register(uploadId, totalRows);
        log.info("Registered uploadId={} with totalRows={}", uploadId, totalRows);

        //  send all rows to Kafka ---
        for (UploadMessage message : messages) {
            kafkaTemplate.send(TOPIC, message);
            log.info("Sent row={} for uploadId={}", message.getRowNumber(), uploadId);
        }

        log.info("Successfully published {} records for uploadId={}", totalRows, uploadId);
        return "Successfully published " + totalRows +
                " records. Use uploadId=" + uploadId + " to download status file.";
    }

    private Long createUploadEntry(Long userId, String fileName) {
        Upload upload = new Upload();
        upload.setUserId(userId);
        upload.setFileName(fileName);
        upload.setStatus("PROCESSING");
        Upload saved = uploadRepository.save(upload);
        return saved.getUploadId();
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return "anonymous";
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }
        throw new RuntimeException("Invalid principal");
    }
}