package com.queuectl.queuectl.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "dlq")
public class DLQItem {
    @Id
    private String id;
    private String originalJobId;
    private Map<String, Object> payload;
    private String reason;
    private String failedAt;
    private int attempts;
    private String type;
}
