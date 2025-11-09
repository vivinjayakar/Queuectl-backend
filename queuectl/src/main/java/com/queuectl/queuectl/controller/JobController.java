package com.queuectl.queuectl.controller;

import com.queuectl.queuectl.dto.CreateJobRequest;
import com.queuectl.queuectl.model.Job;
import com.queuectl.queuectl.service.JobService;
import com.queuectl.queuectl.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    // Create new job (timestamps already handled in JobService)
    @PostMapping
    public ResponseEntity<Job> createJob(@RequestBody CreateJobRequest req) {
        Job created = jobService.createJob(req);
        return ResponseEntity.ok(created);
    }

    // List jobs by state (pending by default)
    @GetMapping
    public ResponseEntity<List<Job>> listJobs(
            @RequestParam(required = false, defaultValue = "pending") String state
    ) {
        return ResponseEntity.ok(jobService.listByState(state));
    }

    // Get single job by ID
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable String id) {
        Job job = jobService.getById(id);
        if (job == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(job);
    }

    // Manual retry: move DLQ item or failed job back to pending queue
    @PostMapping("/{id}/retry")
    public ResponseEntity<?> retryJob(@PathVariable String id) {
        Job job = jobService.getById(id);
        if (job == null) return ResponseEntity.notFound().build();


        job.setNextRunAt(TimeUtils.nowIstString());
        job.setState("pending");
        jobService.save(job);

        return ResponseEntity.ok(Map.of(
                "status", "retry-scheduled",
                "id", id,
                "nextRunAt", job.getNextRunAt()
        ));
    }
}
