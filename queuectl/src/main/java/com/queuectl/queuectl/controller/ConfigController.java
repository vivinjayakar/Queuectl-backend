package com.queuectl.queuectl.controller;

import com.queuectl.queuectl.dto.ConfigRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Value("${queuectl.default.maxRetries:3}")
    private int maxRetries;

    @Value("${queuectl.default.backoffMs:2000}")
    private long backoffMs;

    private volatile int activeWorkers = 0;

    //JSON-based config GET
    @GetMapping
    public Object getConfig() {
        return Map.of(
                "maxRetries", maxRetries,
                "backoffMs", backoffMs
        );
    }

    // JSON-based config POST
    @PostMapping
    public Object setConfig(@RequestBody ConfigRequest req) {
        if (req.getMaxRetries() != null) maxRetries = req.getMaxRetries();
        if (req.getBackoffMs() != null) backoffMs = req.getBackoffMs();
        return Map.of("maxRetries", maxRetries, "backoffMs", backoffMs);
    }

    // CLI endpoint: /api/config/status
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "activeWorkers", activeWorkers,
                "maxRetries", maxRetries,
                "backoffMs", backoffMs
        );
    }

    // CLI endpoint: /api/config/set?key=max-retries&value=3
    @PostMapping("/set")
    public Map<String, Object> setConfigParam(
            @RequestParam String key,
            @RequestParam String value
    ) {
        switch (key) {
            case "max-retries" -> maxRetries = Integer.parseInt(value);
            case "backoff" -> backoffMs = Long.parseLong(value);
            default -> {
                return Map.of("error", "Unknown config key: " + key);
            }
        }
        return Map.of("updatedKey", key, "newValue", value);
    }

    //Setter method for WorkerService to update live count
    public void setActiveWorkers(int count) {
        this.activeWorkers = count;
    }

    //Getter (optional)
    public int getActiveWorkers() {
        return activeWorkers;
    }
}
