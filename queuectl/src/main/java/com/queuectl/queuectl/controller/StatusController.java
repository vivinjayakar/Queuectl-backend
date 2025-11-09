package com.queuectl.queuectl.controller;

import com.queuectl.queuectl.repository.JobRepository;
import com.queuectl.queuectl.repository.DLQRepository;
import com.queuectl.queuectl.controller.ConfigController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/status")
public class StatusController {

    private final JobRepository jobRepository;
    private final DLQRepository dlqRepository;
    private final ConfigController configController;

    @GetMapping
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("pendingJobs", jobRepository.findByState("pending").size());
        status.put("runningJobs", jobRepository.findByState("running").size());
        status.put("succeededJobs", jobRepository.findByState("succeeded").size());
        status.put("failedJobs", jobRepository.findByState("failed").size());
        status.put("dlqJobs", dlqRepository.findAll().size());
        status.put("activeWorkers", configController.getActiveWorkers());
        return status;
    }
}
