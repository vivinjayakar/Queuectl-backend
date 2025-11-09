package com.queuectl.queuectl.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "dead_jobs")
public class DeadJob {
    @Id
    private String id;
    private String type;
    private Map<String, Object> payload;
    private String reason;
    private int attempts;
    private int maxRetries;
    private Instant failedAt;
    private Instant createdAt;
}
