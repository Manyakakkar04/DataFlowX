package com.manyakakkar.DataFlowX.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadMessage {

    private Long uploadId;
    private Long rowNumber;
    private UserDto user;
}