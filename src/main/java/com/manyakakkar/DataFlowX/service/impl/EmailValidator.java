package com.manyakakkar.DataFlowX.service.impl;

import com.manyakakkar.DataFlowX.constants.ErrorCodes;
import com.manyakakkar.DataFlowX.dto.UserDto;
import com.manyakakkar.DataFlowX.service.RowValidators;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmailValidator implements RowValidators {
    @Override
    public void validate(UserDto userDto, List<String> errors) {
        if (userDto.getEmailId() == null || !userDto.getEmailId().contains("@")) {
            errors.add(ErrorCodes.INVALID_EMAIL);
        }
    }
}
