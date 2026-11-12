# Microservices Architecture

## Overview

This project consists of four Spring Boot microservices, PostgreSQL databases, Apache Kafka, a Eureka Naming Server, and an API Gateway.

The API Gateway acts as the single entry point for client HTTP requests, while Eureka handles service registration and discovery. Kafka enables asynchronous event-driven communication between services.

## Services and Ports

| Component | Port / Mapping | Purpose |
|---|---:|---|
| API Gateway | `8765` | Entry point for client HTTP requests |
| Auth Service | `4005` (internal) | Authentication and JWT generation/validation |
| Patient Service | `4000` | Patient management and Kafka event production |
| Billing Service — HTTP | `4001` | Billing HTTP endpoints |
| Billing Service — gRPC | `9001` | Billing gRPC endpoint |
| Analytics Service | `4002:4002` | Analytics and Kafka event consumption |
| Eureka Naming Server | `8761` | Service registration and discovery |
| Auth PostgreSQL | `5001:5432` | Auth Service database |
| Patient PostgreSQL | `5000:5432` | Patient Service database |
| Kafka | `9092:9092`, `9094:9094` | Event-driven communication |

**Note:** Auth Service does not have a published host port. Its internal port is `4005`, accessible to other containers through Docker networking and Eureka discovery.

## Eureka Naming Server

Eureka provides service registration and discovery.

- **Eureka Dashboard:** http://localhost:8761
- **Eureka Registry API:** http://localhost:8761/eureka/apps
- **Eureka registration URL for host-based applications:** `http://localhost:8761/eureka/`
- **Eureka registration URL for containers on the same Docker network:** `http://eureka-naming-server:8761/eureka/`

### Registered Service Discovery Names

| Application Name | Eureka Service ID | Instance Port |
|---|---|---:|
| API Gateway | `api-gateway` | `8765` |
| Auth Service | `auth-service` | `4005` |
| Patient Service | `patient-service` | `4000` |
| Billing Service | `billing-service` | `4001` |
| Analytics Service | `analytics-service` | `4002` |

## API Gateway Routing

The API Gateway uses Spring Cloud Gateway and Eureka-based load balancing.

| Incoming Gateway Path | Eureka Discovery URI | Destination |
|---|---|---|
| `/auth-service/**` | `lb://auth-service` | Auth Service |
| `/patient-service/**` | `lb://patient-service` | Patient Service |
| `/billing-service/**` | `lb://billing-service` | Billing Service |
| `/analytics-service/**` | `lb://analytics-service` | Analytics Service |

### Example API Requests

All requests enter through the API Gateway at `http://localhost:8765`.

```text
http://localhost:8765/auth-service/<endpoint>
http://localhost:8765/patient-service/<endpoint>
http://localhost:8765/billing-service/<endpoint>
http://localhost:8765/analytics-service/<endpoint>
## API Gateway

The API Gateway is the entry point for client-facing HTTP requests. It routes requests to registered microservices using Eureka service discovery and Spring Cloud LoadBalancer.

- **Port:** `8765`
- **Base URL:** http://localhost:8765

## Database

| Component | Host Port | Container Port |
|---|---:|---:|
| Patient Service PostgreSQL | `5000` | `5432` |

The database is accessible from the host machine at `localhost:5000`.

## Kafka

| Kafka Port |
|---:|
| `9092` |
| `9094` |

Kafka supports asynchronous communication between Patient Service and Analytics Service.

## Communication Flow

```text
                 Client / Frontend
                        |
                        v
                  API Gateway
                    Port 8765
                        |
                        v
                 Patient Service
                   Port 4000
                   /         \
                gRPC         Kafka
                 |              |
                 v              v
          Billing Service  Analytics Service
            Port 9001         Port 4002
```

Billing Service also exposes its HTTP endpoint on port `4001`.

## Technology Stack

- Java
- Spring Boot
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Cloud LoadBalancer
- gRPC for synchronous communication between Patient Service and Billing Service
- Apache Kafka for asynchronous event communication
- PostgreSQL for Patient Service persistence
- Docker for containerization

## Configuration Notes

- Eureka Naming Server runs on port `8761`.
- API Gateway runs on port `8765`.
- Patient Service runs on port `4000` and publishes events to Kafka.
- Billing Service exposes HTTP on port `4001` and gRPC on port `9001`.
- Analytics Service runs on port `4002` and consumes Kafka events.
- Patient Service communicates with Billing Service through gRPC.
- Analytics Service receives events from Patient Service through Kafka.
- Services running in Docker on the same network should use Docker service names to communicate with one another.
- Host applications can use `localhost` when the relevant container ports are published.
