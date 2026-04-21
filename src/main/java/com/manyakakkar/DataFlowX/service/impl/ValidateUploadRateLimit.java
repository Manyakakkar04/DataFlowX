package com.manyakakkar.DataFlowX.service.impl;

import com.manyakakkar.DataFlowX.dto.ValidateRequestContext;
import com.manyakakkar.DataFlowX.service.IValidators;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ValidateUploadRateLimit implements IValidators {

    private final StringRedisTemplate redisTemplate;


    @Value("${app.upload.cooldown.minutes}")
    private long uploadCooldownMinutes;


    @Override
    public void validate(ValidateRequestContext context) {

        Long userId = context.id();

        String redisKey = "upload:cooldown:" + userId;

        // ATOMIC: SETNX + TTL
        Boolean allowed = redisTemplate.opsForValue()
                .setIfAbsent(
                        redisKey,
                        "LOCKED",
                        Duration.ofMinutes(uploadCooldownMinutes)
                );

        if (Boolean.FALSE.equals(allowed)) {
            throw new RuntimeException(
                    "You can upload only one file every "
                            + uploadCooldownMinutes + " minutes"
            );


        }
    }
}
