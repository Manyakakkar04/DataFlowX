package com.manyakakkar.DataFlowX.service.impl;

import com.manyakakkar.DataFlowX.constants.ErrorCodes;
import com.manyakakkar.DataFlowX.dto.UserDto;
import com.manyakakkar.DataFlowX.service.RowValidators;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NameValidator implements RowValidators {
    @Override
    public void validate(UserDto userDto, List<String> errors) {
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            errors.add(ErrorCodes.MISSING_DATA);
        }
    }
}
