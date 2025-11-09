# 🚀 QueueCTL Backend Job Queue System  
**A Lightweight Distributed Job Queue built with Spring Boot + MongoDB**

---

## 🧭 Overview  

**QueueCTL** is a background job queue and worker orchestration system built using **Spring Boot** and **MongoDB**.  
It manages background jobs such as sending emails or generating reports using multiple workers that execute jobs **sequentially and efficiently**, with **retry**, **backoff**, and **DLQ (Dead Letter Queue)** mechanisms.

---

## ✨ Features  

- ⚙️ Multi-worker background processing  
- 🧩 Sequential worker execution (Worker-1 → Worker-2 → Worker-3 → …)  
- 💾 MongoDB persistence for all jobs and DLQ  
- 🔁 Automatic retry with exponential backoff  
- 💀 Dead Letter Queue (DLQ) for failed jobs  
- 💬 CLI-based interaction for managing workers, jobs, and configurations  
- 💤 Smart idle state — workers sleep when no jobs are available  
- ⚡ Instant wake-up when new jobs arrive  

---


---

## 🛠️ Tech Stack  

| Component | Technology |
|------------|-------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.x |
| Database | MongoDB |
| Build Tool | Maven |
| CLI | Spring Shell |
| Logging | SLF4J + Logback |

---

## 🧰 Setup Instructions  

### ✅ Prerequisites  
- Java 21 or higher  
- MongoDB running locally (`mongodb://localhost:27017`)  
- Maven installed (`mvn -version`)  

---

### 🏗️ Build & Run Locally  

```bash
# Clone the repository
git clone https://github.com/vivinjayakar/Queuectl-backend.git
cd Queuectl-backend/queuectl

# Build the project
mvn clean package -DskipTests

# Start the backend (API Server)
java -jar target/queuectl-0.0.1-SNAPSHOT.jar


## ⚙️ System Flow  

