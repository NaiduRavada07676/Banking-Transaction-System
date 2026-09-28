# Banking Transaction Backend System

A production-grade, enterprise-level digital banking backend system built with Java 17 and Spring Boot microservices. 

## Architecture & Tech Stack
- **Backend Framework**: Java 17, Spring Boot 3.2.x, Spring Cloud Gateway
- **Database**: PostgreSQL (Relational Data), Redis (Caching)
- **Messaging**: Apache Kafka & Zookeeper (Event-Driven Architecture)
- **Security**: Spring Security, JWT (Role-Based Access Control)
- **DevOps**: Docker, Docker Compose
- **Monitoring**: Prometheus, Grafana, Spring Boot Actuator

## Microservices
1. **API Gateway (`api-gateway`)**: Port 8080. Central routing and JWT validation.
2. **Auth Service (`auth-service`)**: Port 8081. Registration, Login, BCrypt Password Hashing, JWT issuance.
3. **Account Service (`account-service`)**: Port 8082. Manages `Account` state, cached efficiently with Redis.
4. **Transaction Service (`transaction-service`)**: Port 8083. The ledger core. Uses **Pessimistic Locking** (`LockModeType.PESSIMISTIC_WRITE`) to handle highly concurrent transfers safely and prevent double-spending. Features Idempotency keys.
5. **Fraud Detection Service (`fraud-service`)**: Port 8084. Consumes Kafka events asynchronously to flag suspicious activity without slowing down core transactions.
6. **Notification Service (`notification-service`)**: Port 8085. Async email/SMS event processing via Kafka.
7. **Audit Service (`audit-service`)**: Port 8086. Ensures an immutable Kafka-driven log of all operations for compliance.

## GPU Workload Architectural Note (For Fraud Detection)
In standard Java microservice architectures, heavy ML analytics (e.g., neural networks for anomaly detection) aren't run natively in the JVM.
Instead, the `fraud-service` acts as an orchestrator. It receives the Kafka event rapidly and passes the payload via gRPC or REST to a **Python-based microservice running in a separate GPU-enabled Docker Container** utilizing CUDA and TensorFlow/PyTorch. If heavy compute is required for your resume portfolio, you simply attach the Python container to the `docker-compose.yml` with `deploy.resources.reservations.devices.driver=nvidia`.

## How to Run

1. **Start Infrastructure**:
   Ensure Docker is installed and running.
   ```bash
   docker-compose up -d
   ```
   This will spin up Postgres, Redis, Zookeeper, Kafka, Prometheus, and Grafana.

2. **Build the Backend**:
   At the root `banking-system` directory, run:
   ```bash
   mvn clean install
   ```

3. **Run Services**:
   You can run each microservice module via your IDE or execute:
   ```bash
   mvn spring-boot:run -pl api-gateway
   mvn spring-boot:run -pl auth-service
   # ... repeat for other services
   ```

## Production Ready Features Implemented
- **Layered Clean Architecture**: Controllers -> Services -> Repositories -> Entities
- **Concurrency & Locking**: JPA Pessimistic Locking to enforce serializable-level ledger updates.
- **Idempotency**: Prevents executing the same transaction multiple times in case of UI network retries.
- **Microservices Coupling**: Decoupled using Apache Kafka.
- **Prometheus Metric Exposure**: Native actuator mappings for dashboard visualization.
