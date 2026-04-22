package com.manyakakkar.DataFlowX.tracker;
import com.manyakakkar.DataFlowX.dto.RowResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component

public class UploadStatusTracker {
    // one queue per uploadId
    private final ConcurrentHashMap<Long, ConcurrentLinkedQueue<RowResult>> fileQueues
            = new ConcurrentHashMap<>();

    // total rows expected per uploadId
    private final ConcurrentHashMap<Long, Long> fileTotalRows
            = new ConcurrentHashMap<>();
    // register a new file upload before sending to Kafka
    public void register(Long uploadId, Long totalRows) {
        fileQueues.put(uploadId, new ConcurrentLinkedQueue<>());
        fileTotalRows.put(uploadId, totalRows);
        log.info("Registered uploadId={} with totalRows={}", uploadId, totalRows);
    }
    // called by each consumer thread to push a row result
    public void addResult(Long uploadId, RowResult result) {
        ConcurrentLinkedQueue<RowResult> queue = fileQueues.get(uploadId);
        if (queue != null) {
            queue.add(result);
        } else {
            log.warn("No queue found for uploadId={}", uploadId);
        }
    }

    // called by writer thread to drain the queue
    public ConcurrentLinkedQueue<RowResult> getQueue(Long uploadId) {
        return fileQueues.get(uploadId);
    }

    // called by writer thread to check if all rows are processed
    public Long getTotalRows(Long uploadId) {
        return fileTotalRows.get(uploadId);
    }
    // called after file is written — clean up memory
    public void deregister(Long uploadId) {
        fileQueues.remove(uploadId);
        fileTotalRows.remove(uploadId);
        log.info("Deregistered uploadId={} from tracker", uploadId);
    }

    // called by writer thread to know which files to process
    public Set<Long> getAllUploadIds() {
        return fileQueues.keySet();
    }

}
