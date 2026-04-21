package com.manyakakkar.DataFlowX.service;

import com.manyakakkar.DataFlowX.dto.UserDto;

import java.util.List;

public interface RowValidators {
    public void validate(UserDto userDto, List<String> errors);
}
