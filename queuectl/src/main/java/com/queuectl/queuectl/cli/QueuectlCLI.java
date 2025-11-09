package com.queuectl.queuectl.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import java.net.http.*;
import java.net.URI;
import java.util.concurrent.Callable;

@Command(
        name = "queuectl",
        description = "QueueCTL Command-Line Interface",
        subcommands = {
                EnqueueCommand.class,
                WorkerCommands.class,
                ListCommand.class,
                DLQCommands.class,
                StatusCommand.class,
                ConfigCommands.class
        }
)
public class QueuectlCLI implements Callable<Integer> {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new QueuectlCLI()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        System.out.println("QueueCTL CLI - use `queuectl --help` for commands.");
        return 0;
    }


    static String sendGet(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    static void sendPost(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    static void sendPostJson(String url, String json) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}

/*ENQUEUE */
@Command(name = "enqueue", description = "Add a new job to the queue")
class EnqueueCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "JSON string representing job payload")
    private String jsonPayload;

    public Integer call() throws Exception {
        String url = "http://localhost:8081/api/jobs";
        QueuectlCLI.sendPostJson(url, jsonPayload);
        System.out.println("Enqueued new job: " + jsonPayload);
        return 0;
    }
}

/* WORKERS  */
@Command(name = "worker", description = "Manage workers",
        subcommands = {WorkerStart.class, WorkerStop.class})
class WorkerCommands {}

@Command(name = "start", description = "Start one or more workers")
class WorkerStart implements Callable<Integer> {

    @Option(names = "--count", description = "Number of workers", defaultValue = "1")
    int count;

    @Option(names = "--pollMs", description = "Polling interval (ms)", defaultValue = "500")
    long pollMs;

    public Integer call() throws Exception {
        String url = "http://localhost:8081/api/workers/start?count=" + count + "&pollMs=" + pollMs;
        QueuectlCLI.sendPost(url);
        System.out.println("Started " + count + " worker(s)");
        return 0;
    }
}

@Command(name = "stop", description = "Stop all running workers gracefully")
class WorkerStop implements Callable<Integer> {
    public Integer call() throws Exception {
        QueuectlCLI.sendPost("http://localhost:8081/api/workers/stop");
        System.out.println("Workers stopped successfully.");
        return 0;
    }
}

/*LIST */
@Command(name = "list", description = "List jobs by state")
class ListCommand implements Callable<Integer> {

    @Option(names = "--state", description = "Job state (pending/succeeded/failed)", defaultValue = "pending")
    String state;

    public Integer call() throws Exception {
        String url = "http://localhost:8081/api/jobs?state=" + state;
        String response = QueuectlCLI.sendGet(url);
        System.out.println("Jobs (" + state + "):\n" + response);
        return 0;
    }
}

/*DLQ  */
@Command(name = "dlq", description = "Manage Dead Letter Queue",
        subcommands = {DLQList.class, DLQRetry.class})
class DLQCommands {}

@Command(name = "list", description = "View DLQ jobs")
class DLQList implements Callable<Integer> {
    public Integer call() throws Exception {
        String response = QueuectlCLI.sendGet("http://localhost:8081/api/dlq");
        System.out.println("DLQ Jobs:\n" + response);
        return 0;
    }
}

@Command(name = "retry", description = "Retry a DLQ job by ID")
class DLQRetry implements Callable<Integer> {

    @Parameters(index = "0", description = "DLQ job ID to retry")
    String jobId;

    public Integer call() throws Exception {
        String url = "http://localhost:8081/api/dlq/" + jobId + "/retry";
        QueuectlCLI.sendPost(url);
        System.out.println("Retried DLQ job: " + jobId);
        return 0;
    }
}

/* STATUS */
@Command(name = "status", description = "Show summary of job states & active workers")
class StatusCommand implements Callable<Integer> {
    public Integer call() throws Exception {
        String response = QueuectlCLI.sendGet("http://localhost:8081/api/config/status");
        System.out.println("Queue Status:\n" + response);
        return 0;
    }
}

/*CONFIG  */
@Command(name = "config", description = "Manage configuration (retry, backoff, etc.)",
        subcommands = {ConfigSet.class})
class ConfigCommands {}

@Command(name = "set", description = "Set configuration parameters like max-retries or backoff")
class ConfigSet implements Callable<Integer> {

    @Parameters(index = "0", description = "Configuration key (e.g., max-retries)")
    String key;

    @Parameters(index = "1", description = "Configuration value (e.g., 3)")
    String value;

    public Integer call() throws Exception {
        String url = "http://localhost:8081/api/config/set?key=" + key + "&value=" + value;
        QueuectlCLI.sendPost(url);
        System.out.println("Updated config: " + key + " = " + value);
        return 0;
    }
}
