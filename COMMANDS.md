# QueueCTL Command Reference

This document lists all commands used for running, testing, and verifying the QueueCTL backend system.

---

## 1. Build the Project
```bash
mvn clean package -DskipTests
This command compiles the project and builds the executable JAR file inside the target/ directory.


2. Start the Backend Server (Terminal 1)

# 1. Start backend server
java -jar target/queuectl-0.0.1-SNAPSHOT.jar

# 2. Open another terminal and start workers
java -jar target/queuectl-0.0.1-SNAPSHOT-cli.jar worker start --count=3

# 3. Enqueue jobs
java -jar target/queuectl-0.0.1-SNAPSHOT-cli.jar enqueue "{\"type\":\"send-email\",\"payload\":{\"to\":\"user1@example.com\"}}"
java -jar target/queuectl-0.0.1-SNAPSHOT-cli.jar enqueue "{\"type\":\"send-email\",\"payload\":{\"to\":\"user2@example.com\"}}"

# 4. Verify jobs
java -jar target/queuectl-0.0.1-SNAPSHOT-cli.jar list --state=succeeded

# 5. Check DLQ (if any failed jobs exist)
java -jar target/queuectl-0.0.1-SNAPSHOT-cli.jar dlq list

# 6. Retry DLQ job (if needed)
java -jar target/queuectl-0.0.1-SNAPSHOT-cli.jar dlq retry <job-id>

# 7. Stop workers
java -jar target/queuectl-0.0.1-SNAPSHOT-cli.jar worker stop
