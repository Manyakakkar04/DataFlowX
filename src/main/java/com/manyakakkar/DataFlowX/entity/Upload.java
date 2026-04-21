package com.manyakakkar.DataFlowX.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "Uploads")
public class Upload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uploadId;
    private Long userId;
    String fileName;
    String status;


}
