package com.manyakakkar.DataFlowX.service.impl;

import com.manyakakkar.DataFlowX.constants.ErrorCodes;
import com.manyakakkar.DataFlowX.dto.RowResult;
import com.manyakakkar.DataFlowX.dto.UploadMessage;
import com.manyakakkar.DataFlowX.dto.UserDto;
import com.manyakakkar.DataFlowX.entity.User;
import com.manyakakkar.DataFlowX.tracker.UploadStatusTracker;
import com.manyakakkar.DataFlowX.repository.UserRepository;
import com.manyakakkar.DataFlowX.service.ConsumerService;
import com.manyakakkar.DataFlowX.service.RowValidators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerServiceImpl implements ConsumerService {

    private final UserRepository userRepository;
    private final UploadStatusTracker uploadStatusTracker;
    private final List<RowValidators> rowValidators;

    @Override
    @KafkaListener(
            topics = "bulk-upload-topic",
            groupId = "bulk-group"

    )
    public void processData(List<UploadMessage> messages) {

        if (messages == null || messages.isEmpty()) {
            log.warn("Received empty batch from Kafka.");
            return;
        }

        List<User> validUsers = new ArrayList<>();

        for (UploadMessage msg : messages) {

            if (msg == null) continue;

            Long uploadId = msg.getUploadId();
            Long rowNumber = msg.getRowNumber();
            UserDto userDto = msg.getUser();

            String status;
            List<String> errors = new ArrayList<>();

            try {
                if (userDto == null) {
                    errors.add(ErrorCodes.MISSING_DATA);
                } else {
                    for (RowValidators validator : rowValidators) {
                        validator.validate(userDto, errors);
                    }
                }

                if (errors.isEmpty()) {
                    status = "SUCCESS";

                    User user = new User();
                    user.setName(userDto.getName());
                    user.setEmailId(userDto.getEmailId());
                    user.setMobile(userDto.getMobile());
                    validUsers.add(user);

                } else {
                    status = "FAILED";
                }

            } catch (Exception e) {
                status = "FAILED";
                errors.add(ErrorCodes.PROCESSING_ERROR);
                log.error("Error processing row {} for uploadId={}: {}",
                        rowNumber, uploadId, e.getMessage());
            }

            // push to in-memory queue
            RowResult rowResult = new RowResult(
                    rowNumber,
                    status,
                    errors.isEmpty() ? null : String.join(" | ", errors)
            );
            uploadStatusTracker.addResult(uploadId, rowResult);
            log.debug("Pushed RowResult to queue — uploadId={}, row={}, status={}",
                    uploadId, rowNumber, status);
        }

        // save valid users to DB in batch as before
        if (!validUsers.isEmpty()) {
            log.info("Saving batch of {} valid users to DB", validUsers.size());
            userRepository.saveAll(validUsers);
            log.info("Batch insert completed.");
        } else {
            log.warn("No valid users in this batch.");
        }
    }
}