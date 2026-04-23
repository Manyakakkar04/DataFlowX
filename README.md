# DataFlowX 🚀

A high-performance, distributed, event-driven data pipeline built with **Apache Kafka** and **Spring Boot**, capable of ingesting and processing **100,000+ records** in parallel.

---

## 📌 Overview

DataFlowX is a backend system designed for large-scale data ingestion. It leverages Kafka's distributed messaging to fan out workloads across parallel consumers, persists processed data to PostgreSQL, and uses Redis for fast caching. Secure REST APIs with JWT-based authentication control access to the pipeline.

---

## 🏗️ Architecture

```
[CSV / REST Input]
        │
        ▼
[Spring Boot API Layer]  ──── JWT Auth (Spring Security)
        │
        ▼
[Kafka Producer]
        │
   ─────┴─────
   │         │         │
[Consumer] [Consumer] [Consumer]   ← Parallel processing
   │         │         │
   └────┬────┘
        ▼
  [PostgreSQL DB]
        │
        ▼
   [Redis Cache]
```

---

## ⚙️ Tech Stack

| Layer             | Technology                          |
|-------------------|-------------------------------------|
| Language          | Java 17                             |
| Framework         | Spring Boot 3.4.5                   |
| Messaging         | Apache Kafka (Spring Kafka)         |
| Database          | PostgreSQL (Spring Data JPA)        |
| Caching           | Redis (Spring Data Redis)           |
| Security          | Spring Security + JWT (JJWT 0.11.5) |
| CSV Parsing       | OpenCSV 5.9                         |
| Utilities         | Lombok, Apache Commons IO           |
| Build Tool        | Maven                               |

---

## ✨ Features

- **High-throughput ingestion** — handles 100k+ records via Kafka-backed parallel consumers
- **Event-driven architecture** — decoupled producers and consumers for resilient data flow
- **CSV & REST input** — ingest data from file uploads or API calls
- **JWT-secured endpoints** — stateless authentication for all pipeline APIs
- **Redis caching** — reduce DB load on frequently accessed data
- **PostgreSQL persistence** — reliable, relational storage for processed records
- **Lombok + clean code** — minimal boilerplate across the codebase

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Apache Kafka (running locally or via Docker)
- PostgreSQL
- Redis

### 1. Clone the repository

```bash
git clone https://github.com/Manyakakkar04/DataFlowX.git
cd DataFlowX
```

### 2. Configure the application

Edit `src/main/resources/application.properties` (or `application.yml`) with your environment values:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/dataflowx
spring.datasource.username=your_username
spring.datasource.password=your_password

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=dataflowx-group

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT
jwt.secret=your_jwt_secret_key
jwt.expiration=86400000
```

### 3. Start Kafka (Docker)

```bash
docker run -d --name zookeeper -p 2181:2181 zookeeper
docker run -d --name kafka -p 9092:9092 \
  --link zookeeper \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  confluentinc/cp-kafka
```

### 4. Build and run

```bash
./mvnw clean install
./mvnw spring-boot:run
```

---

## 📡 API Endpoints

| Method | Endpoint            | Description                        | Auth Required |
|--------|---------------------|------------------------------------|---------------|
| POST   | `/auth/register`    | Register a new user                | ❌             |
| POST   | `/auth/login`       | Login and receive JWT token        | ❌             |
| POST   | `/api/ingest`       | Ingest a single record via API     | ✅             |
| POST   | `/api/ingest/csv`   | Upload and ingest a CSV file       | ✅             |
| GET    | `/api/records`      | Fetch processed records            | ✅             |

> Include the JWT token in the `Authorization: Bearer <token>` header for protected routes.


---

## 📁 Project Structure

```
DataFlowX/
├── src/
│   ├── main/
│   │   ├── java/com/manyakakkar/DataFlowX/
│   │   │   ├── config/          # Kafka, Security, Redis config
│   │   │   ├── controller/      # REST API controllers
│   │   │   ├── producer/        # Kafka producers
│   │   │   ├── consumer/        # Kafka consumers (parallel)
│   │   │   ├── service/         # Business logic
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── model/           # Entity & DTO classes
│   │   │   └── security/        # JWT filter & auth logic
│   │   └── resources/
│   │       └── application.properties
├── pom.xml
└── README.md
```



---

*Built with ☕ Java and a love for distributed systems.*
