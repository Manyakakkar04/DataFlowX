package com.manyakakkar.DataFlowX.service;


import com.manyakakkar.DataFlowX.dto.UploadMessage;
import com.manyakakkar.DataFlowX.dto.UserDto;

import java.util.List;

public interface ConsumerService {
    void processData(List<UploadMessage> messages);
}
