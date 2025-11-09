package com.queuectl.queuectl.controller;

import com.queuectl.queuectl.model.DLQItem;
import com.queuectl.queuectl.repository.DLQRepository;
import com.queuectl.queuectl.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dlq")
@RequiredArgsConstructor
public class DLQController {

    private final DLQRepository dlqRepository;
    private final JobService jobService;

    @GetMapping
    public List<DLQItem> list() {
        return dlqRepository.findAll();
    }

    // Retry DLQ item by copying it back to jobs (creates a new job)
    @PostMapping("/{id}/retry")
    public Object retry(@PathVariable String id) {
        return dlqRepository.findById(id).map(item -> {
            // create job from DLQ item
            com.queuectl.queuectl.dto.CreateJobRequest req = new com.queuectl.queuectl.dto.CreateJobRequest();
            req.setType(item.getType());
            req.setPayload(item.getPayload());
            req.setMaxRetries(3);
            req.setBackoffMs(2000L);
            jobService.createJob(req);
            return Map.of("status", "queued", "dlqId", id);
        }).orElse(Map.of("error", "not-found"));
    }
}
