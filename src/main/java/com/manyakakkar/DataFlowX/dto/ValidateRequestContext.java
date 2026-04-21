package com.manyakakkar.DataFlowX.dto;

import org.springframework.web.multipart.MultipartFile;

public record ValidateRequestContext(MultipartFile file,Long id) {
    //records can not extend another class
   // Not ideal when : You need setters

}
