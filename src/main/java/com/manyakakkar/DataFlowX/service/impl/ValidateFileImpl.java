package com.manyakakkar.DataFlowX.util.impl;

import com.manyakakkar.DataFlowX.dto.ValidateRequestContext;
import com.manyakakkar.DataFlowX.service.IValidators;
import com.manyakakkar.DataFlowX.service.ValidateFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ValidateFileImpl implements IValidators {


    @Override
    public void validate(ValidateRequestContext context) {
        MultipartFile file = context.file();
        if (file == null) {
            throw new RuntimeException("File is null");
        }

        // Empty file check
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // File name check
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new RuntimeException("Invalid file name");
        }

        // File type check
        if (!fileName.toLowerCase().endsWith(".csv")) {
            throw new RuntimeException("Only CSV files allowed");
        }


    }
}