package com.queuectl.queuectl.service;

import com.queuectl.queuectl.controller.ConfigController;
import com.queuectl.queuectl.model.Job;
import com.queuectl.queuectl.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkerService {

    private final ClaimService claimService;
    private final JobService jobService;
    private final DLQService dlqService;
    private final ConfigController configController;

    private ExecutorService workerPool;
    private volatile boolean running = false;
    private int workerCount = 0;

    private static final Random RAND = new Random();
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition turnCondition = lock.newCondition();
    private final Condition jobAvailable = lock.newCondition();

    private int currentTurn = 1;
    private boolean jobPresent = false;

    //Start workers sequentially
    public synchronized String start(int workerCount, long pollIntervalMs) {
        if (running) return "already-running";
        running = true;
        this.workerCount = workerCount;
        this.currentTurn = 1;

        workerPool = Executors.newFixedThreadPool(workerCount);
        configController.setActiveWorkers(workerCount);

        log.info("Starting {} workers sequentially...", workerCount);
        for (int i = 1; i <= workerCount; i++) {
            final int workerId = i;
            workerPool.submit(() -> runWorkerLoop(workerId));
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("All workers started successfully ({} total)", workerCount);
        return "started";
    }

    // Stop workers
    public synchronized String stop() {
        if (!running) return "not-running";
        running = false;
        lock.lock();
        try {
            turnCondition.signalAll();
            jobAvailable.signalAll();
        } finally {
            lock.unlock();
        }
        if (workerPool != null) workerPool.shutdownNow();
        configController.setActiveWorkers(0);
        log.info("Workers stopped.");
        return "stopped";
    }

    @PreDestroy
    public void cleanup() {
        stop();
    }

    private void runWorkerLoop(int workerId) {
        String workerName = "Worker-" + workerId;
        log.info("{} started and waiting for jobs...", workerName);
        try {
            while (running) {
                lock.lock();
                try {
                    // Wait for my turn
                    while (running && currentTurn != workerId) {
                        turnCondition.await();
                    }
                    if (!running) break;

                    // Wait for available job
                    while (running && !jobPresent) {
                        log.info("{} idle - waiting for new job...", workerName);
                        jobAvailable.await();
                    }
                    if (!running) break;

                    Job job = claimService.claimNextJob();
                    if (job == null) {
                        // No job available after wake → go idle again
                        jobPresent = false;
                        continue;
                    }

                    // Process job outside lock to let others wait
                    lock.unlock();
                    process(job, workerName);
                    lock.lock();

                    // Move turn to next worker
                    currentTurn = (workerId % workerCount) + 1;
                    log.info("➡️ Turn given to Worker-{}", currentTurn);
                    turnCondition.signalAll();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("⚠️ {} interrupted.", workerName);
                    break;
                } finally {
                    if (lock.isHeldByCurrentThread()) lock.unlock();
                }
            }
        } catch (Exception e) {
            log.error("{} loop error: {}", workerName, e.getMessage(), e);
        } finally {
            log.info("{} stopped.", workerName);
        }
    }

    public void signalNewJob() {
        lock.lock();
        try {
            jobPresent = true;
            jobAvailable.signal();
            log.info("Job signal sent — waking Worker-{}.", currentTurn);
        } finally {
            lock.unlock();
        }
    }

    private void process(Job job, String workerName) {
        boolean success = false;
        String errorMsg = null;
        try {
            Map<String, Object> payload = job.getPayload();
            if (payload != null && Boolean.TRUE.equals(payload.get("forceFail")))
                throw new RuntimeException("forced-failure");

            Thread.sleep(500);
            success = RAND.nextInt(100) < 95;
            if (!success) throw new RuntimeException("random-failure");

        } catch (Exception ex) {
            errorMsg = ex.getMessage();
        }

        if (success) {
            job.setState("succeeded");
            job.setUpdatedAt(TimeUtils.nowIstString());
            jobService.save(job);
            log.info("{} completed job id={} successfully.", workerName, job.getId());
        } else {
            handleFailure(job, errorMsg, workerName);
        }
    }

    private void handleFailure(Job job, String errorMsg, String workerName) {
        // Increment attempt count first
        int attempts = job.getAttempts() + 1;
        job.setAttempts(attempts);
        job.setLastError(errorMsg);

        log.info("{} handling failure for job id={} (attempt {}/{})", workerName, job.getId(), attempts, job.getMaxRetries());

        if (attempts < job.getMaxRetries()) {
            // Retry with exponential backoff
            long delay = Math.min(job.getBackoffMs() * (1L << Math.max(0, attempts - 1)), 60000L);
            job.setNextRunAt(TimeUtils.nowIstString());
            job.setState("pending");
            job.setUpdatedAt(TimeUtils.nowIstString());
            jobService.save(job);
            log.warn("{} retrying job id={} (attempt {}/{}) after {}s",
                    workerName, job.getId(), attempts, job.getMaxRetries(), delay / 1000);
        } else {
            // Push to DLQ once max retries are reached
            dlqService.push(job, "max-retries-exhausted: " + errorMsg);
            job.setState("failed");
            job.setUpdatedAt(TimeUtils.nowIstString());
            jobService.save(job);
            log.error("{} moved job id={} to DLQ after {} attempts", workerName, job.getId(), attempts);
        }
    }
}
