# Microservices + ELK Development Plan (API Gateway, Eureka, User/Order/Payment, Search)

This guide gives a practical **step-by-step process** to evolve this repository into a Spring Cloud microservices system with centralized logging and Elasticsearch-based search.

## 1) Target Architecture

Services:

1. `discovery-service` (Eureka Server)
2. `api-gateway` (Spring Cloud Gateway)
3. `user-service`
4. `order-service`
5. `payment-service`
6. `search-service` (queries Elasticsearch)
7. ELK stack: Elasticsearch + Logstash + Kibana

High-level flow:

- All business services register in Eureka.
- API Gateway routes external traffic to internal services using service discovery.
- Services emit logs in a structured format.
- Logstash receives logs and forwards to Elasticsearch.
- Kibana visualizes logs.
- `search-service` provides APIs to query/order/search data using Elasticsearch.

---

## 2) Prerequisites

- Java 21
- Maven 3.9+
- Docker + Docker Compose
- 8GB+ RAM recommended (ELK + multiple services)

---

## 3) Repository Layout to Create

```text
Distributed-Logs-System-
├── discovery-service/
├── api-gateway/
├── user-service/
├── order-service/
├── payment-service/
├── search-service/
├── shared-observability/
│   ├── log4j2.xml
│   └── log-patterns.md
├── docker/
│   ├── logstash/
│   └── elk/
├── docker-compose.yml
└── docs/
    └── microservices-elk-step-by-step.md
```

Tip: keep each service as an independent Spring Boot app with its own `pom.xml` and `src/`.

---

## 4) Build Discovery Service (Eureka)

1. Generate a Spring Boot project (`discovery-service`) with:
   - Spring Web
   - Eureka Server
   - Actuator
2. Add `@EnableEurekaServer` in the main class.
3. Configure `application.yml`:

```yaml
server:
  port: 8761

spring:
  application:
    name: discovery-service

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

4. Start and verify: `http://localhost:8761`.

---

## 5) Build API Gateway

1. Generate `api-gateway` with:
   - Spring Cloud Gateway
   - Eureka Discovery Client
   - Actuator
2. Configure routes by logical service names:

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
        - id: payment-service
          uri: lb://payment-service
          predicates:
            - Path=/api/payments/**
        - id: search-service
          uri: lb://search-service
          predicates:
            - Path=/api/search/**

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

3. Add basic global filter for request logging and trace id propagation.

---

## 6) Build Domain Services

Create three services with consistent patterns.

### 6.1 user-service

Endpoints:

- `POST /api/users`
- `GET /api/users/{id}`
- `GET /api/users`

Use DB of your choice (H2/PostgreSQL) and emit events/logs for user creation.

### 6.2 order-service

Endpoints:

- `POST /api/orders`
- `GET /api/orders/{id}`
- `GET /api/orders?userId=...`

When creating an order:

- validate user (REST call to `user-service`)
- request payment authorization from `payment-service`
- persist order status and log lifecycle transitions

### 6.3 payment-service

Endpoints:

- `POST /api/payments/authorize`
- `POST /api/payments/capture`
- `GET /api/payments/{id}`

Store transaction status and include structured error logs for failed payments.

---

## 7) Build search-service (Elasticsearch API)

1. Generate `search-service` with:
   - Spring Web
   - Spring Data Elasticsearch (or Elasticsearch Java client)
   - Eureka Client
2. Add APIs:

- `GET /api/search/logs?service=user-service&level=ERROR`
- `GET /api/search/orders?q=...`

3. Recommended index strategy:

- `app-logs-*` for centralized logs
- `orders-*` for searchable order documents

4. Return paginated responses to avoid large payloads.

---

## 8) Standardize Logging in All Services

1. Use a shared Log4j2 pattern with service name and correlation id:

```text
%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - app=%X{appName} traceId=%X{traceId} %msg%n
```

2. Ensure every service sets `spring.application.name`.
3. Add request filter/interceptor to put `traceId` into MDC.
4. Send logs to Logstash via TCP appender (`localhost:5000` in local dev).

---

## 9) Configure Logstash Parsing

Update pipeline to parse the additional fields (`app`, `traceId`, message). Example grok pattern concept:

```text
%{TIMESTAMP_ISO8601:timestamp} \[%{DATA:thread}\] %{LOGLEVEL:log_level} %{JAVACLASS:logger} - app=%{DATA:app_name} traceId=%{DATA:trace_id} %{GREEDYDATA:log_message}
```

Normalize fields and ship to index:

- `microservices-logs-%{+YYYY.MM.dd}`

---

## 10) Docker Compose Orchestration

Use `docker-compose.yml` for infrastructure:

- Elasticsearch
- Logstash
- Kibana
- optionally each microservice container

If running services locally from IDE, keep only ELK in compose and run services via Maven.

---

## 11) Recommended Implementation Order

1. Start ELK infrastructure (already present in this repo).
2. Implement `discovery-service`.
3. Implement `api-gateway` with one route.
4. Implement `user-service` and register it in Eureka.
5. Implement `order-service` and integrate with user.
6. Implement `payment-service` and integrate with order flow.
7. Apply unified logging config to all services.
8. Implement `search-service` with basic Elasticsearch query API.
9. Add resilience:
   - timeouts/retries (Resilience4j)
   - fallback responses
10. Add monitoring and health checks through Actuator.

---

## 12) Validation Checklist

- Eureka dashboard shows all services as `UP`.
- Gateway routes requests to all services.
- Create user → create order → authorize payment happy path works.
- Failure scenario (payment failed) is logged and searchable in Kibana.
- Search API returns filtered log/order data from Elasticsearch.
- Each log entry includes `app_name` and `trace_id`.

---

## 13) Useful Commands

```bash
# Infra
docker compose up -d

# Run services (example)
cd discovery-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd search-service && mvn spring-boot:run

# Quick checks
curl http://localhost:8761
curl http://localhost:8080/api/users
curl "http://localhost:8080/api/search/logs?service=order-service&level=ERROR"
```

---

## 14) Next Enhancements

- Add distributed tracing (OpenTelemetry + Jaeger/Tempo).
- Use Kafka for event-driven order/payment workflows.
- Add centralized configuration (Spring Cloud Config).
- Add security (JWT at API Gateway, service-to-service auth).
