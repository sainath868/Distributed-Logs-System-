# Spring Boot Microservices + Eureka + Gateway + MySQL + ELK + Docker Compose

Complete microservices architecture with:
- Eureka Server (`discovery-server`, port `8761`)
- Spring Cloud Gateway (`api-gateway`, port `8080`)
- `user-service` (`8081`)
- `product-service` (`8082`)
- `order-service` (`8083`)
- Spring Data JPA + MySQL
- ELK stack logging (Logstash + Elasticsearch + Kibana)
- Docker Compose for full local orchestration

## Project Structure

```text
.
├── pom.xml
├── docker-compose.yml
├── docker/
│   └── logstash/
│       ├── config/logstash.yml
│       └── pipeline/logstash.conf
├── discovery-server/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/discovery/DiscoveryServerApplication.java
│       └── resources/
│           ├── application.yml
│           └── logback-spring.xml
├── api-gateway/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/gateway/ApiGatewayApplication.java
│       └── resources/
│           ├── application.yml
│           └── logback-spring.xml
├── user-service/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/user/
│       │   ├── UserServiceApplication.java
│       │   ├── controller/UserController.java
│       │   ├── entity/User.java
│       │   └── repository/UserRepository.java
│       └── resources/
│           ├── application.yml
│           └── logback-spring.xml
├── product-service/
│   └── ... (Product entity, repository, controller)
└── order-service/
    └── ... (OrderEntity entity, repository, controller)
```

## Full Maven Dependencies

### Root `pom.xml`
- Spring Boot BOM `3.3.5`
- Spring Cloud BOM `2023.0.3`
- Modules:
  - `discovery-server`
  - `api-gateway`
  - `user-service`
  - `product-service`
  - `order-service`

### `discovery-server`
- `spring-boot-starter-web`
- `spring-cloud-starter-netflix-eureka-server`
- `spring-boot-starter-actuator`
- `logstash-logback-encoder`

### `api-gateway`
- `spring-cloud-starter-gateway`
- `spring-cloud-starter-netflix-eureka-client`
- `spring-boot-starter-actuator`
- `logstash-logback-encoder`

### `user-service`, `product-service`, `order-service`
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-cloud-starter-netflix-eureka-client`
- `spring-boot-starter-actuator`
- `mysql-connector-j`
- `logstash-logback-encoder`
- `spring-boot-starter-test`

## Gateway Routes

Configured in `api-gateway/src/main/resources/application.yml`:
- `/users/**` -> `lb://user-service`
- `/products/**` -> `lb://product-service`
- `/orders/**` -> `lb://order-service`

## Key `application.yml` Configuration

- `discovery-server`: runs Eureka on `8761`, no self-registration
- `api-gateway`: runs on `8080`, route predicates for users/products/orders
- service ports:
  - user: `8081`
  - product: `8082`
  - order: `8083`
- MySQL JDBC URLs:
  - `userdb`, `productdb`, `orderdb`
- all clients register to:
  - `http://discovery-server:8761/eureka/`

## Entities

- `User`: `id`, `name`, `email`
- `Product`: `id`, `name`, `description`, `price`
- `OrderEntity`: `id`, `userId`, `productId`, `quantity`

Each service includes:
- `@Entity`
- `JpaRepository`
- REST controller with endpoints:
  - `POST /<resource>`
  - `GET /<resource>`
  - `GET /<resource>/{id}`

## Logback Configuration

`logback-spring.xml` is included in all services and gateway/discovery:
- console output appender
- `LogstashTcpSocketAppender` to `logstash:5000`
- structured JSON payload through `LogstashEncoder`

## Docker Compose

`docker-compose.yml` includes:
- `mysql`
- `discovery-server`
- `api-gateway`
- `user-service`
- `product-service`
- `order-service`
- `elasticsearch`
- `logstash`
- `kibana`

## Logstash Pipeline

`docker/logstash/pipeline/logstash.conf`:
- TCP JSON input on `5000`
- optional app/service enrichment
- output to Elasticsearch index:
  - `microservices-logs-%{+YYYY.MM.dd}`

## Run

```bash
mvn clean package
docker compose up --build
```

URLs:
- Eureka: http://localhost:8761
- Gateway: http://localhost:8080
- User service direct: http://localhost:8081
- Product service direct: http://localhost:8082
- Order service direct: http://localhost:8083
- Elasticsearch: http://localhost:9200
- Kibana: http://localhost:5601
