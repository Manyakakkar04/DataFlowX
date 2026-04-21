package com.manyakakkar.DataFlowX.controller;

import com.manyakakkar.DataFlowX.service.impl.ProducerServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final ProducerServiceImpl producerService;

    // TODO: integrate login functionality, using JWT token --DONE
    // TODO: user should not be able to upload file without login --DONE
    // TODO: Manya should not be able to upload same file twice --WILL DO LATER
    // TODO: Manya should not be able to upload file in 5 minutes --DONE

    @PostMapping("/file")
    public ResponseEntity<String> publishToKafka(@RequestParam("file") MultipartFile file){

        log.info("Upload request received");
       // user should not be able to upload same file again immediately or never in future?
        // TODO: implement batching --DONE
        // TODO: implement logback.xml | log4j.xml --DONE

        String result= producerService.publishToKafka(file);
        log.info("Upload request processed, response={}", result);

        return ResponseEntity.ok(result);

    }

    // TODO: Manya should not be able to download status file uploaded by Atul
    // TODO: give the feature to user for downloading status file
    // TODO: implement the status file

}