# 🏦 Banking Transaction Backend System

A production-oriented **digital banking backend system** built using **Java 17, Spring Boot, Spring Cloud, PostgreSQL, Redis, Apache Kafka, JWT, Docker, Prometheus, and Grafana**.

The system follows a **microservices-based architecture** designed to demonstrate secure authentication, concurrent transaction processing, event-driven communication, caching, fraud detection, notifications, and audit logging.

---

# 🚀 Tech Stack

| Category         | Technology              |
| ---------------- | ----------------------- |
| Language         | Java 17                 |
| Backend          | Spring Boot 3.2.x       |
| Architecture     | Microservices           |
| API Gateway      | Spring Cloud Gateway    |
| Database         | PostgreSQL              |
| Cache            | Redis                   |
| Messaging        | Apache Kafka            |
| Coordination     | Zookeeper               |
| Security         | Spring Security + JWT   |
| Password Hashing | BCrypt                  |
| Containerization | Docker + Docker Compose |
| Monitoring       | Spring Boot Actuator    |
| Metrics          | Prometheus              |
| Visualization    | Grafana                 |
| API Testing      | Postman                 |
| Build Tool       | Maven                   |

---

# 🏗️ System Architecture

```text
                         ┌─────────────────────┐
                         │       Client        │
                         │   Web / Mobile App  │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    API Gateway      │
                         │       :8080         │
                         │ JWT Validation      │
                         │ Request Routing     │
                         └──────────┬──────────┘
                                    │
             ┌──────────────────────┼──────────────────────┐
             │                      │                      │
             ▼                      ▼                      ▼
      ┌─────────────┐       ┌─────────────┐       ┌─────────────┐
      │ Auth Service│       │Account       │       │ Transaction │
      │    :8081    │       │Service :8082│       │Service :8083│
      └─────────────┘       └─────────────┘       └──────┬──────┘
             │                      │                    │
             │                      ▼                    │
             │                  ┌────────┐              │
             │                  │ Redis   │              │
             │                  └────────┘              │
             │                                           │
             │                                           ▼
             │                                    ┌────────────┐
             │                                    │   Kafka    │
             │                                    │ Event Bus  │
             │                                    └─────┬──────┘
             │                                          │
             │                    ┌─────────────────────┼────────────────────┐
             │                    │                     │                    │
             ▼                    ▼                     ▼                    ▼
      ┌─────────────┐      ┌─────────────┐      ┌─────────────┐      ┌─────────────┐
      │    Fraud    │      │Notification │      │    Audit    │      │ Other       │
      │  :8084      │      │   :8085     │      │   :8086     │      │ Consumers   │
      └─────────────┘      └─────────────┘      └─────────────┘      └─────────────┘
             │
             ▼
      ┌──────────────────┐
      │ Python ML Service│
      │ Optional GPU     │
      │ TensorFlow/PyTorch│
      └──────────────────┘
```

---

# 🔐 Authentication Flow

The authentication system uses **Spring Security, BCrypt, and JWT**.

```text
User
 │
 │ Login
 ▼
API Gateway
 │
 ▼
Auth Service
 │
 ├── Validate Credentials
 │
 ├── BCrypt Password Check
 │
 └── Generate JWT
        │
        ▼
      Client
        │
        │ Authorization: Bearer <JWT>
        ▼
   API Gateway
        │
        ├── Validate JWT
        │
        ▼
    Microservice
```

### Security Features

* JWT authentication
* Role-based authorization
* BCrypt password hashing
* Protected endpoints
* Token validation
* Authentication filters
* Gateway-level security

---

# 💳 Transaction Processing

The **Transaction Service** is the core component responsible for transferring money between accounts.

```text
Client
  │
  │ POST /transactions/transfer
  ▼
API Gateway
  │
  │ JWT Validation
  ▼
Transaction Service
  │
  ├── Validate Request
  │
  ├── Check Idempotency Key
  │
  ├── Load Source Account
  │
  ├── Load Destination Account
  │
  ├── Acquire Pessimistic Lock
  │
  ├── Validate Balance
  │
  ├── Debit Source
  │
  ├── Credit Destination
  │
  ├── Commit Transaction
  │
  └── Publish Kafka Event
             │
             ▼
           Kafka
        ┌────┼─────┐
        ▼    ▼     ▼
      Fraud Notification Audit
```

---

# 🔒 Concurrency Control

The transaction service uses **JPA pessimistic locking** when updating account balances.

Example:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Account a WHERE a.id = :id")
Optional<Account> findAccountForUpdate(
        @Param("id") Long id
);
```

This allows the application to lock the relevant database row during the transaction.

### Objective

```text
Transaction A
      │
      ▼
Account Row Locked
      │
      ▼
Balance Updated
      │
      ▼
Transaction Committed
      │
      ▼
Lock Released
```

A concurrent transaction must wait until the existing database transaction releases the lock.

---

# ♻️ Idempotency

The transaction API supports **idempotency keys** to protect against duplicate transaction requests.

Example:

```http
POST /api/transactions/transfer
Idempotency-Key: TXN-2026-000001
```

If the client accidentally sends the same request multiple times:

```text
Request 1
   ↓
Process Transaction
   ↓
Store Idempotency Key
   ↓
Transaction Completed

Request 2
   ↓
Same Idempotency Key
   ↓
Already Processed
   ↓
Return Existing Result
```

This is useful when network failures cause clients to retry requests.

---

# 📨 Event-Driven Architecture

Apache Kafka is used to decouple transaction processing from downstream operations.

### Transaction Event

```text
Transaction Service
        │
        ▼
Kafka Topic
"transaction-events"
        │
        ├───────────────┐
        │               │
        ▼               ▼
Fraud Service     Audit Service
        │
        ▼
Notification Service
```

### Benefits

* Loose coupling
* Asynchronous processing
* Independent scaling
* Reliable event distribution
* Faster transaction response
* Easier integration of additional consumers

---

# 🚨 Fraud Detection

The Fraud Detection Service consumes transaction events asynchronously.

```text
Transaction Event
       │
       ▼
     Kafka
       │
       ▼
Fraud Detection Service
       │
       ├── Rule Validation
       │
       ├── Transaction Analysis
       │
       └── Optional ML Service
                 │
                 ▼
          Fraud Risk Result
```

For advanced ML workloads, the Java service can communicate with a separate Python service.

```text
Java Fraud Service
        │
        │ REST / gRPC
        ▼
Python ML Service
        │
        ├── TensorFlow
        ├── PyTorch
        └── GPU / CUDA
```

The ML service can return information such as:

```json
{
  "transactionId": "TXN-10001",
  "riskScore": 0.87,
  "riskLevel": "HIGH",
  "flagged": true
}
```

The exact fraud model and thresholds should be configured according to the application's requirements.

---

# 📧 Notification Service

The Notification Service processes events asynchronously.

```text
Kafka
  │
  ▼
Notification Service
  │
  ├── Transaction Success
  ├── Transaction Failure
  ├── Fraud Alert
  └── Account Events
          │
          ▼
    Email / SMS Provider
```

This prevents notification processing from unnecessarily blocking the core transaction request.

---

# 📝 Audit Service

The Audit Service maintains an event-driven audit trail.

```text
Microservices
      │
      ▼
    Kafka
      │
      ▼
 Audit Service
      │
      ▼
Audit Database
```

Audit events can contain:

```text
Event ID
User ID
Transaction ID
Event Type
Timestamp
Service Name
Operation
Status
Request Metadata
```

This provides traceability for important system operations.

---

# ⚡ Redis Caching

Redis is used by the Account Service for frequently accessed account information.

```text
Client
  │
  ▼
Account Service
  │
  ├── Check Redis
  │      │
  │      ├── Cache Hit ──► Return Data
  │      │
  │      └── Cache Miss
  │              │
  │              ▼
  │          PostgreSQL
  │              │
  │              ▼
  │          Store in Redis
  │              │
  │              ▼
  │          Return Data
```

Caching reduces repeated database reads for suitable read-heavy operations.

---

# 📊 Monitoring

The system exposes application metrics using:

* Spring Boot Actuator
* Prometheus
* Grafana

Architecture:

```text
Microservices
      │
      ▼
Spring Boot Actuator
      │
      ▼
Prometheus
      │
      ▼
Grafana Dashboard
```

Example metrics:

```text
HTTP Request Count
HTTP Response Time
JVM Memory
CPU Usage
Database Connections
Kafka Consumer Metrics
Error Count
Application Health
```

---

# 🐳 Docker Architecture

The complete infrastructure can be started using Docker Compose.

```text
docker-compose
      │
      ├── PostgreSQL
      ├── Redis
      ├── Zookeeper
      ├── Kafka
      ├── Prometheus
      ├── Grafana
      │
      ├── API Gateway
      ├── Auth Service
      ├── Account Service
      ├── Transaction Service
      ├── Fraud Service
      ├── Notification Service
      └── Audit Service
```

---

# 📦 Microservices

| Service              | Port | Responsibility                |
| -------------------- | ---: | ----------------------------- |
| API Gateway          | 8080 | Routing & JWT validation      |
| Auth Service         | 8081 | Registration & authentication |
| Account Service      | 8082 | Account management            |
| Transaction Service  | 8083 | Money transfers & ledger      |
| Fraud Service        | 8084 | Fraud analysis                |
| Notification Service | 8085 | Notifications                 |
| Audit Service        | 8086 | Audit events                  |

---

# 🗄️ Core Data Model

```text
User
 │
 ├── Role
 │
 └── Account
       │
       ├── Balance
       ├── Account Number
       └── Status
              │
              ▼
        Transaction
        ├── Sender
        ├── Receiver
        ├── Amount
        ├── Type
        ├── Status
        ├── Idempotency Key
        └── Timestamp
```

---

# 📁 Project Structure

```text
banking-system/
│
├── api-gateway/
│   └── src/
│
├── auth-service/
│   └── src/
│
├── account-service/
│   └── src/
│
├── transaction-service/
│   └── src/
│
├── fraud-service/
│   └── src/
│
├── notification-service/
│   └── src/
│
├── audit-service/
│   └── src/
│
├── fraud-ml-service/
│   └── src/
│
├── docker-compose.yml
├── pom.xml
└── README.md
```

Each Spring Boot service follows a layered structure:

```text
service/
└── src/main/java/com/banking/
    ├── controller/
    ├── service/
    ├── repository/
    ├── entity/
    ├── dto/
    ├── security/
    ├── exception/
    ├── config/
    └── kafka/
```

---

# 🌐 API Examples

### Authentication

```http
POST /api/auth/register
POST /api/auth/login
```

### Accounts

```http
POST /api/accounts
GET /api/accounts/{id}
GET /api/accounts/{id}/balance
```

### Transactions

```http
POST /api/transactions/transfer
GET /api/transactions/{id}
GET /api/transactions/account/{accountId}
```

### Admin

```http
GET /api/admin/users
GET /api/admin/transactions
GET /api/admin/audit-logs
```

---

# 🧪 Testing

Testing can be performed using:

* JUnit 5
* Mockito
* Spring Boot Test
* MockMvc
* Postman
* Integration Tests

Important test scenarios include:

* Successful login
* Invalid credentials
* JWT validation
* Account creation
* Successful transfer
* Insufficient balance
* Concurrent transfers
* Duplicate idempotency requests
* Kafka event publishing
* Fraud event processing
* Exception handling

---

# ▶️ How to Run

## 1. Clone Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd banking-system
```

## 2. Start Infrastructure

Make sure Docker is installed and running.

```bash
docker compose up -d
```

This starts:

```text
PostgreSQL
Redis
Kafka
Zookeeper
Prometheus
Grafana
```

## 3. Build the Project

```bash
mvn clean install
```

## 4. Run Services

```bash
mvn spring-boot:run -pl api-gateway
mvn spring-boot:run -pl auth-service
mvn spring-boot:run -pl account-service
mvn spring-boot:run -pl transaction-service
mvn spring-boot:run -pl fraud-service
mvn spring-boot:run -pl notification-service
mvn spring-boot:run -pl audit-service
```

---

# 📈 Production-Oriented Features

The project demonstrates:

* Microservices architecture
* API Gateway
* JWT authentication
* Role-based authorization
* BCrypt password hashing
* REST APIs
* PostgreSQL persistence
* Redis caching
* Apache Kafka messaging
* Event-driven architecture
* Pessimistic database locking
* Idempotent transaction processing
* Asynchronous fraud detection
* Notification processing
* Audit logging
* Docker containerization
* Prometheus monitoring
* Grafana dashboards
* Spring Boot Actuator
* Unit and integration testing

---

# 🔮 Future Enhancements

Potential extensions include:

* Kubernetes deployment
* Service discovery
* Distributed tracing
* OpenTelemetry
* Resilience4j circuit breakers
* API rate limiting
* Secrets management
* CI/CD pipeline
* Cloud deployment
* Advanced fraud ML models
* Real-time transaction dashboard
* Email/SMS provider integration
* Automated database migrations using Flyway
* Kafka Schema Registry
* Dead-letter queues

---

# 👨‍💻 Author

**Ravada Sanyasi Naidu**

B.Tech – Artificial Intelligence & Machine Learning
Mohan Babu University, Tirupati

---

# ⭐ Key Project Flow

```text
                    CLIENT
                       │
                       ▼
                ┌──────────────┐
                │ API GATEWAY  │
                │ JWT Security │
                └──────┬───────┘
                       │
       ┌───────────────┼────────────────┐
       │               │                │
       ▼               ▼                ▼
     AUTH           ACCOUNT       TRANSACTION
   SERVICE          SERVICE         SERVICE
                       │                │
                       ▼                ▼
                    REDIS             KAFKA
                                         │
                     ┌───────────────────┼──────────────────┐
                     │                   │                  │
                     ▼                   ▼                  ▼
                   FRAUD            NOTIFICATION           AUDIT
                   SERVICE             SERVICE             SERVICE
                     │
                     ▼
                  ML SERVICE
                Python / GPU
```

---

## 💡 Backend Architecture Highlight

The most important technical flow in this project is:

**JWT → API Gateway → Transaction Service → Pessimistic Locking → PostgreSQL → Kafka → Fraud / Notification / Audit**

This gives you several strong topics to explain in a technical interview: **microservices, Spring Security, JWT, JPA locking, concurrency, idempotency, Kafka, Redis, Docker, and monitoring**.
