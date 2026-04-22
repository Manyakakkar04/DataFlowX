package com.manyakakkar.DataFlowX.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@Slf4j
@RestController
@RequestMapping("/download")
public class DownloadController {

    @Value("${status.files.dir}")
    private String statusFilesDir;

    @GetMapping("/status/{uploadId}")
    public ResponseEntity<Resource> downloadStatusFile(
            @PathVariable Long uploadId) {

        log.info("Download request received for uploadId={}", uploadId);

        File file = new File(statusFilesDir + "/status_" + uploadId + ".csv");

        // file not found
        if (!file.exists()) {
            log.warn("Status file not found for uploadId={}", uploadId);
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        log.info("status file for uploadId={}", uploadId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=status_" + uploadId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
}
