package com.manyakakkar.DataFlowX.service;


import org.springframework.web.multipart.MultipartFile;

public interface ProducerService {
    String publishToKafka(MultipartFile file);
}
