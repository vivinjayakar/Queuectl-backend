package com.queuectl.queuectl.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CreateJobRequest {
    private String type;
    private Map<String, Object> payload;
    private Integer maxRetries;
    private Long backoffMs;
    // optional: nextRunAt string can be added; we keep simple
}
