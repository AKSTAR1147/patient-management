# Microservices Architecture

## Overview

This project consists of three Spring Boot microservices, a PostgreSQL database, Kafka, and a Eureka Naming Server for service discovery.

### Services and Ports

| Component | Host Port | Purpose |
|---|---:|---|
| Patient Service | `4000` | Patient management and Kafka event production |
| Billing Service | `4001` | Billing service HTTP endpoint |
| Billing Service | `9001` | Billing service gRPC endpoint |
| Analytics Service | `4002` | Analytics and Kafka event consumption |
| Eureka Naming Server | `8761` | Service registration and discovery |

### Database

| Component | Host Port | Container Port |
|---|---:|---:|
| Patient Service PostgreSQL | `5000` | `5432` |

The database is accessible from the host machine at `localhost:5000`.

### Kafka

| Component | Port |
|---|---:|
| Kafka | `9092` |
| Kafka | `9094` |

Kafka is used for asynchronous communication between Patient Service and Analytics Service.

### Communication Flow

```text
                 Client / Frontend
                        |
                        v
                  API Gateway
                        |
                        v
                 Patient Service
                  Port 4000
                   /       \
                  /         \
             gRPC             Kafka
               |                |
               v                v
        Billing Service   Analytics Service
          Port 9001          Port 4002
```

Billing Service also exposes its HTTP endpoint on port `4001`.

### Endpoint Documentation

Endpoint paths and HTTP methods will be documented here as they are provided.

#### Patient Service — Port 4000

| Method | Endpoint | Description |
|---|---|---|
| TBD | TBD | To be documented |

#### Billing Service — Port 4001

| Method | Endpoint | Description |
|---|---|---|
| TBD | TBD | To be documented |

#### Billing Service — gRPC Port 9001

| Service / Method | Description |
|---|---|
| TBD | To be documented |

#### Analytics Service — Port 4002

| Method | Endpoint | Description |
|---|---|---|
| TBD | TBD | To be documented |

### Technology Stack

- Java and Spring Boot
- Spring Cloud Netflix Eureka
- gRPC for synchronous communication between Patient Service and Billing Service
- Apache Kafka for asynchronous event communication
- PostgreSQL for Patient Service persistence
- Docker for containerization

### Configuration Notes

- Eureka Naming Server runs on port `8761`.
- Patient Service publishes events to Kafka.
- Analytics Service consumes events from Kafka.
- Patient Service communicates with Billing Service through gRPC.
- Service endpoint details and Docker deployment configuration will be added as they become available.
