package com.manyakakkar.DataFlowX.service.impl;

import com.manyakakkar.DataFlowX.constants.ErrorCodes;
import com.manyakakkar.DataFlowX.dto.UploadMessage;
import com.manyakakkar.DataFlowX.dto.UserDto;
import com.manyakakkar.DataFlowX.entity.RowResult;
import com.manyakakkar.DataFlowX.entity.User;
import com.manyakakkar.DataFlowX.repository.RowResultRepository;
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
    private final RowResultRepository rowResultRepository;

    private final List<RowValidators> rowValidators;

    @Override
    @KafkaListener(
            topics = "bulk-upload-topic",
            groupId = "bulk-group"
    )
    public void processData(List<UploadMessage> messages) {

        if (messages == null || messages.isEmpty()) {
            log.warn("Received empty message from Kafka.");
            return;
        }

       // log.info("Consumer received {} user records from Kafka.", users.size());

        List<User> validUsers = new ArrayList<>();
        List<RowResult> rowResults = new ArrayList<>();

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
                }
                else{
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

                log.error("Error processing row {} for uploadId {}: {}",
                        rowNumber, uploadId, e.getMessage());
            }

            RowResult row = new RowResult();
            row.setUploadId(uploadId);
            row.setRowNumber(rowNumber);
            row.setName(userDto != null ? userDto.getName() : null);
            row.setEmail(userDto != null ? userDto.getEmailId() : null);
            row.setMobile(userDto != null ? userDto.getMobile() : null);
            row.setStatus(status);
            row.setErrorMessage(String.join(",", errors));

            rowResults.add(row);
        }


        if (!validUsers.isEmpty()) {
            log.info("Saving batch of {} users to DB ", validUsers.size());
            userRepository.saveAll(validUsers);
            log.info("Batch insert completed successfully.");
        } else {
            log.warn("No valid users found to save into DB.");
        }
        if (!rowResults.isEmpty()) {
            rowResultRepository.saveAll(rowResults);
        }

    }
}
