package com.queuectl.queuectl.controller;

import com.queuectl.queuectl.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @PostMapping("/start")
    public String startWorkers(@RequestParam(defaultValue = "1") int count,
                               @RequestParam(defaultValue = "1000") long pollMs) {
        return workerService.start(count, pollMs);
    }

    @PostMapping("/stop")
    public String stopWorkers() {
        return workerService.stop();
    }
}
