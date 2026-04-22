package com.manyakakkar.DataFlowX.service;
import com.manyakakkar.DataFlowX.dto.RowResult;
import com.manyakakkar.DataFlowX.tracker.UploadStatusTracker;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusFileWriter {

    private final UploadStatusTracker uploadStatusTracker;


    @Value("${status.files.dir}")
    private String statusFilesDir;

    private static final int BATCH_SIZE = 500;

    // tracks how many rows have been written per uploadId
    private final ConcurrentHashMap<Long, Long> writtenRowsCount
            = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 500)
    public void drainQueues() {

        for (Long uploadId : uploadStatusTracker.getAllUploadIds()) {

            ConcurrentLinkedQueue<RowResult> queue =
                    uploadStatusTracker.getQueue(uploadId);

            if (queue == null || queue.isEmpty()) continue;
            // drain up to BATCH_SIZE rows at a time
            List<RowResult> batch = new ArrayList<>();
            RowResult result;
            int count = 0;

            while ((result = queue.poll()) != null && count < BATCH_SIZE) {
                batch.add(result);
                count++;
            }

            if (batch.isEmpty()) continue;

            // sort by row number before writing
            batch.sort(Comparator.comparingLong(RowResult::getRowNumber));

            // write batch to CSV
            writeToCSV(uploadId, batch);

            // update written rows count
            long totalWritten = writtenRowsCount.merge(
                    uploadId, (long) batch.size(), Long::sum
            );


            log.info("Written {} rows so far for uploadId={}",
                    totalWritten, uploadId);

            // check if all rows are done
            Long totalRows = uploadStatusTracker.getTotalRows(uploadId);
            if (totalRows != null && totalWritten >= totalRows) {
                finalizeFile(uploadId, totalWritten, totalRows);
            }
        }
    }

    private void writeToCSV(Long uploadId, List<RowResult> batch) {

        // create directory if it doesn't exist
        File dir = new File(statusFilesDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //it does not create a new file It creates a Java File object that represents a path in the file system.
        //File as a pointer or reference to a path
        File file = new File(statusFilesDir + "/status_" + uploadId + ".csv");
        boolean isNewFile = !file.exists();

        try (CSVWriter writer = new CSVWriter(new FileWriter(file, true))) {

            // write header only once when file is first created
            if (isNewFile) {
                writer.writeNext(new String[]{
                        "row_number", "status", "error_message"
                });
            }
            // write each row in the batch
            for (RowResult row : batch) {
                writer.writeNext(new String[]{
                        String.valueOf(row.getRowNumber()),
                        row.getStatus(),
                        row.getErrorMessage() != null ? row.getErrorMessage() : ""
                });
            }

        } catch (IOException e) {
            log.error("Error writing status CSV for uploadId={}: {}",
                    uploadId, e.getMessage());
        }
    }

    private void finalizeFile(Long uploadId, long totalWritten, long totalRows) {
        if (totalWritten == totalRows) {
            log.info("Status file complete for uploadId={} — all {} rows written",
                    uploadId, totalRows);
        } else {
            log.warn("Status file for uploadId={} has {} rows but expected {}",
                    uploadId, totalWritten, totalRows);
        }

        // clean up memory
        uploadStatusTracker.deregister(uploadId);
        writtenRowsCount.remove(uploadId);

        log.info("Cleanup done for uploadId={}", uploadId);
    }
}

