package com.manyakakkar.DataFlowX.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RowResult {
    private Long rowNumber;
    private String status;       // SUCCESS or FAILED
    private String errorMessage; // null if success
}
// we are not making it as entity