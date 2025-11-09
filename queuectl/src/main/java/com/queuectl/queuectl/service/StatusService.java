package com.queuectl.queuectl.service;

import com.queuectl.queuectl.controller.ConfigController;
import com.queuectl.queuectl.repository.JobRepository;
import com.queuectl.queuectl.repository.DLQRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final JobRepository jobRepository;
    private final DLQRepository dlqRepository;
    private final ConfigController configController;

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();

        long pending = jobRepository.findByState("pending").size();
        long running = jobRepository.findByState("running").size();
        long succeeded = jobRepository.findByState("succeeded").size();
        long failed = jobRepository.findByState("failed").size();
        long dlq = dlqRepository.count();
        int workers = configController.getActiveWorkers();

        status.put("pending", pending);
        status.put("running", running);
        status.put("succeeded", succeeded);
        status.put("failed", failed);
        status.put("dlq", dlq);
        status.put("activeWorkers", workers);

        return status;
    }
}
