package com.manyakakkar.DataFlowX.util.impl;

import com.manyakakkar.DataFlowX.service.ValidateFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ValidateFileImpl implements ValidateFile {

    @Override
    public boolean check(MultipartFile file) {

        if (file == null) {
            return false;
        }

        // Empty file check
        if (file.isEmpty()) {
            return false;
        }

        // File name check
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            return false;
        }

        // File type check
        if (!fileName.toLowerCase().endsWith(".csv")) {
            return false;
        }

        return true;
    }
}