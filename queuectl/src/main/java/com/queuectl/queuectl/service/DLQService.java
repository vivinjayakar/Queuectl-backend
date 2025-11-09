package com.queuectl.queuectl.service;

import com.queuectl.queuectl.model.DLQItem;
import com.queuectl.queuectl.model.Job;
import com.queuectl.queuectl.repository.DLQRepository;
import com.queuectl.queuectl.repository.JobRepository;
import com.queuectl.queuectl.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DLQService {

    private final DLQRepository dlqRepository;
    private final JobRepository jobRepository; //for retry support


    public DLQItem push(Job job, String reason) {
        DLQItem item = DLQItem.builder()
                .originalJobId(job.getId())
                .payload(job.getPayload())
                .reason(reason)
                .failedAt(TimeUtils.nowIstString())
                .attempts(job.getAttempts())
                .type(job.getType())
                .build();

        DLQItem saved = dlqRepository.save(item);
        System.out.println("Moved to DLQ: " + saved.getOriginalJobId() + " (" + reason + ")");
        return saved;
    }


    public List<DLQItem> listAll() {
        List<DLQItem> items = dlqRepository.findAll();
        if (items.isEmpty()) {
            System.out.println("🕳️ DLQ is empty — no failed jobs to show.");
        } else {
            System.out.println("📜 Dead Letter Queue Items:");
            items.forEach(item -> System.out.printf(
                    "💀 ID=%s | Type=%s | Reason=%s | Attempts=%d | FailedAt=%s%n",
                    item.getOriginalJobId(),
                    item.getType(),
                    item.getReason(),
                    item.getAttempts(),
                    item.getFailedAt()
            ));
        }
        return items;
    }


    public void retry(String jobId) {
        DLQItem item = dlqRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("No DLQ entry found for id=" + jobId));


        Job retriedJob = Job.builder()
                .id(item.getOriginalJobId())
                .type(item.getType())
                .payload(item.getPayload())
                .state("pending")
                .attempts(0)
                .maxRetries(3) // default — can be customized
                .backoffMs(2000)
                .nextRunAt(TimeUtils.nowIstString())
                .createdAt(TimeUtils.nowIstString())
                .updatedAt(TimeUtils.nowIstString())
                .build();

        jobRepository.save(retriedJob);
        dlqRepository.deleteById(jobId);

        System.out.println("Retried DLQ job: " + retriedJob.getId() + " (moved back to queue)");
    }


    public void clearAll() {
        dlqRepository.deleteAll();
        System.out.println("Cleared all items from DLQ.");
    }
}
