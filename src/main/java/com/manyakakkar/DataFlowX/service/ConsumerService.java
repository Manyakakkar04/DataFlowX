package com.manyakakkar.DataFlowX.service;


import com.manyakakkar.DataFlowX.dto.UserDto;

public interface ConsumerService {
    void processData(UserDto userDto);
}
