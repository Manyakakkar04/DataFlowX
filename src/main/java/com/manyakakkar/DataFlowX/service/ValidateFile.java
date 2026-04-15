package com.manyakakkar.DataFlowX.service;

import org.springframework.web.multipart.MultipartFile;

public interface ValidateFile {
    boolean check(MultipartFile file);

}
