package com.queuectl.queuectl.service;

import com.queuectl.queuectl.dto.CreateJobRequest;
import com.queuectl.queuectl.model.Job;
import com.queuectl.queuectl.repository.JobRepository;
import com.queuectl.queuectl.util.TimeUtils;
import com.queuectl.queuectl.config.ApplicationContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    @Value("${queuectl.default.maxRetries:3}")
    private int defaultMaxRetries;

    @Value("${queuectl.default.backoffMs:2000}")
    private long defaultBackoffMs;


    public Job createJob(CreateJobRequest req) {
        int maxRetries = req.getMaxRetries() != null ? req.getMaxRetries() : defaultMaxRetries;
        long backoff = req.getBackoffMs() != null ? req.getBackoffMs() : defaultBackoffMs;

        // Check for duplicates
        boolean duplicateExists = jobRepository.existsByTypeAndPayloadAndStateIn(
                req.getType(),
                req.getPayload(),
                List.of("pending", "running")
        );

        if (duplicateExists) {
            System.out.println("⚠️ Duplicate job detected — skipping enqueue for type="
                    + req.getType() + ", payload=" + req.getPayload());
            return null; // skip saving duplicate job
        }

        //Create new job
        Job job = Job.builder()
                .type(req.getType())
                .payload(req.getPayload())
                .state("pending")
                .attempts(0)
                .maxRetries(maxRetries)
                .backoffMs(backoff)
                .nextRunAt(TimeUtils.nowIstString())
                .createdAt(TimeUtils.nowIstString())
                .updatedAt(TimeUtils.nowIstString())
                .build();

        Job saved = jobRepository.save(job);

        // Wake up idle workers immediately
        try {
            WorkerService workerService = ApplicationContextProvider.getBean(WorkerService.class);
            workerService.signalNewJob();
        } catch (Exception e) {
            System.err.println("Failed to signal workers after job enqueue: " + e.getMessage());
        }

        System.out.println("📬 Job enqueued successfully: " + saved.getId() + " (" + saved.getType() + ")");
        return saved;
    }

    public List<Job> listAllJobs() {
        return jobRepository.findAll();
    }

    public List<Job> listByState(String state) {
        return jobRepository.findByState(state);
    }

    public Job getById(String id) {
        return jobRepository.findById(id).orElse(null);
    }

    public Job save(Job job) {
        job.setUpdatedAt(TimeUtils.nowIstString());
        return jobRepository.save(job);
    }

    public Job retryFailedJob(String jobId) {
        Job job = getById(jobId);
        if (job == null) {
            throw new IllegalStateException("Job not found for ID: " + jobId);
        }
        if (!"failed".equals(job.getState())) {
            throw new IllegalStateException("Job " + jobId + " is not in failed state. Current state: " + job.getState());
        }

        job.setState("pending");
        job.setAttempts(0);
        job.setNextRunAt(TimeUtils.nowIstString());
        job.setUpdatedAt(TimeUtils.nowIstString());
        jobRepository.save(job);

        System.out.println("Retried failed job: " + jobId);

        try {
            WorkerService workerService = ApplicationContextProvider.getBean(WorkerService.class);
            workerService.signalNewJob();
        } catch (Exception e) {
            System.err.println("Failed to signal workers after retry: " + e.getMessage());
        }

        return job;
    }

    public Job markAsSucceeded(String jobId) {
        Job job = getById(jobId);
        if (job == null) throw new IllegalStateException("Job not found: " + jobId);
        job.setState("succeeded");
        job.setUpdatedAt(TimeUtils.nowIstString());
        jobRepository.save(job);
        System.out.println("Job manually marked succeeded: " + jobId);
        return job;
    }

    public void deleteJob(String id) {
        jobRepository.deleteById(id);
        System.out.println("Deleted job: " + id);
    }

    public long countByState(String state) {
        return jobRepository.findByState(state).size();
    }
}

