package com.queuectl.queuectl.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "jobs")
public class Job {

    @Id
    private String id;

    private String type;
    private Map<String, Object> payload;

    @Indexed
    private String state;

    private int attempts;
    private int maxRetries;
    private long backoffMs;

    @Indexed
    private String nextRunAt;
    private String createdAt;
    private String updatedAt;
    private String lockedAt;
    private String lastError;
}
