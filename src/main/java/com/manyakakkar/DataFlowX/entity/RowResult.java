package com.manyakakkar.DataFlowX.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class RowResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rowId;
    private Long uploadId;
    private Long rowNumber;
    private String name;
    private String email;
    private Long mobile;
    private String status;
    private String errorMessage;


}
