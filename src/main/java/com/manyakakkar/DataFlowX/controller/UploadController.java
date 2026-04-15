package com.manyakakkar.DataFlowX.controller;

import com.manyakakkar.DataFlowX.service.ProducerService;
import com.manyakakkar.DataFlowX.service.ValidateFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {
    private final ProducerService producerService;
    private final ValidateFile validateFile;


    @PostMapping("/file")
    public ResponseEntity<String> publishToKafka(@RequestParam("file") MultipartFile file){
        if (!validateFile.check(file)) {
            return ResponseEntity.badRequest().body("Invalid file");
        }

        String result= producerService.publishToKafka(file);

        return ResponseEntity.ok(result);

    }

}
