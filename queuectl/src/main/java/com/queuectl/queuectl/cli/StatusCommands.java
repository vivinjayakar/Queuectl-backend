package com.queuectl.queuectl.cli;

import com.queuectl.queuectl.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.Map;

@ShellComponent
@RequiredArgsConstructor
public class StatusCommands {

    private final StatusService statusService;

    @ShellMethod(key = "status", value = "Show summary of all job states & active workers")
    public String showStatus() {
        Map<String, Object> status = statusService.getStatus();

        StringBuilder sb = new StringBuilder();
        sb.append("\n Queue Status:\n");
        sb.append("---------------------------\n");
        sb.append("Pending Jobs:     ").append(status.get("pending")).append("\n");
        sb.append("Running Jobs:     ").append(status.get("running")).append("\n");
        sb.append("Succeeded Jobs:   ").append(status.get("succeeded")).append("\n");
        sb.append("Failed Jobs:      ").append(status.get("failed")).append("\n");
        sb.append("DLQ Jobs:         ").append(status.get("dlq")).append("\n");
        sb.append("Active Workers:   ").append(status.get("activeWorkers")).append("\n");
        sb.append("---------------------------\n");
        return sb.toString();
    }
}
