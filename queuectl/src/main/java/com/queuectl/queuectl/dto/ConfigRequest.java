package com.queuectl.queuectl.dto;

import lombok.Data;

@Data
public class ConfigRequest {
    private Integer maxRetries;
    private Long backoffMs;
}
