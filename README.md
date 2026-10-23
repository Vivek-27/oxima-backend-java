# Oxima: Real-Time Ride-Hailing Backend Microservice (Java 17 & Spring Boot)

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg?style=flat&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![Redis Geospatial](https://img.shields.io/badge/Redis-Geospatial-red.svg?style=flat&logo=redis)](https://redis.io/)
[![Apache Kafka](https://img.shields.io/badge/Apache-Kafka-black.svg?style=flat&logo=apachekafka)](https://kafka.apache.org/)
[![Status](https://img.shields.io/badge/Production-Live%20Backend-brightgreen.svg)]()

> High-throughput, distributed ride-hailing backend service engineered with **Java 17**, **Spring Boot**, **Redis Geospatial**, and **Apache Kafka**. Powers real-time driver telemetry, sub-50ms proximity matchmaking, dynamic surge pricing, and fault-tolerant ride lifecycle state machines.

---

## ⚡ Architecture Overview

```
[ Rider / Driver Mobile & Web Apps ]
                 │
                 ▼ (REST / WebSockets)
   ┌───────────────────────────┐
   │ Spring Boot Microservice  │
   │  • Spring Security (JWT)  │
   │  • WebSocket Handlers     │
   │  • Ride Lifecycle State   │
   └─────────────┬─────────────┘
                 │
        ┌────────┴────────┐
        ▼                 ▼
 ┌──────────────┐  ┌──────────────┐
 │ Redis Spatial│  │ Apache Kafka │
 │ • GEOADD     │  │ • Telemetry  │
 │ • GEORADIUS  │  │ • Ride Events│
 │ • TTL Caches │  │ • Dispatch   │
 └──────────────┘  └──────────────┘
        │                 │
        └────────┬────────┘
                 ▼
     ┌───────────────────────┐
     │ PostgreSQL / MongoDB  │
     │ Persistence Layer     │
     └───────────────────────┘
```

---

## 🚀 Key Technical Highlights

- **Sub-50ms Proximity Matching**: Utilizes Redis geospatial indices (`GEOADD`, `GEORADIUSBYMEMBER`) to query nearby available drivers within configurable radius (3km - 10km).
- **Decoupled Telemetry Streaming**: Ingests continuous GPS coordinate pings from drivers into partition-keyed Apache Kafka topics, guaranteeing in-order processing.
- **WebSocket Synchronization**: Pushes instant state updates (Ride Requested, Accepted, Arrived, In-Transit, Completed) to connected rider and driver clients over secure STOMP / WebSocket channels.
- **PIN-Lock Ride Security**: Requires 4-digit proximity-validated handshake PIN verification within 20 meters before ride start.
- **Enterprise Architecture**: Spring Data JPA, DTO mappers, custom validation frameworks, global `@ControllerAdvice` exception handling, and JUnit 5 / Mockito unit test coverage.

---

## 🛠️ Tech Stack

- **Core**: Java 17, Spring Boot, Spring Web, Spring Security, Spring WebSocket
- **Caching & Spatial**: Redis, Lettuce driver, Redis Geospatial (`GEOSEARCH`)
- **Messaging**: Apache Kafka, Kafka Streams
- **Database**: PostgreSQL / MongoDB, Hibernate ORM
- **Testing**: JUnit 5, Mockito, Testcontainers

---

## 📦 Getting Started

### Prerequisites
- JDK 17+
- Maven 3.8+
- Docker & Docker Compose (for Redis and Kafka)

### Run with Maven
```bash
# Start Redis and Kafka
docker run -d -p 6379:6379 redis:alpine

# Build & Run
./mvnw clean install
./mvnw spring-boot:run
```

---

## 👤 Author
**Vivek Kumar**  
Full-Stack & Distributed Systems Engineer &bull; Dual AWS Certified (Developer & Data Engineer)  
- LinkedIn: [linkedin.com/in/vivek-fullstack](https://www.linkedin.com/in/vivek-fullstack/)  
- GitHub: [github.com/Vivek-27](https://github.com/Vivek-27)
