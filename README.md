# QueueCTL Backend Job Queue System  
**Built with Spring Boot + MongoDB**

## Overview  
QueueCTL is a distributed job queue system built using **Java (Spring Boot)** and **MongoDB**.  
It manages background jobs with retry policies, DLQ (Dead Letter Queue), and concurrent worker processing.

## Setup Instructions  

### Prerequisites
- Java 21+ installed  
- MongoDB running locally (`mongodb://localhost:27017`)  
- Maven installed (`mvn -version`)

### Run Locally

```bash
# Clone the repository
git clone https://github.com/vivinjayakar/Queuectl-backend.git
cd Queuectl-backend/queuectl

# Build the project
mvn clean package -DskipTests

# Start the backend (API Server)
java -jar target/queuectl-0.0.1-SNAPSHOT.jar

