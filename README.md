# Spring Boot + ELK Stack Centralized Logging (Java 21)

This project demonstrates a complete centralized logging pipeline:

1. Spring Boot app generates logs with **Log4j2**.
2. Logs are shipped to **Logstash** over TCP.
3. Logstash parses logs using **Grok**.
4. Parsed logs are forwarded to **Elasticsearch**.
5. Elasticsearch stores logs in daily indices (`spring-boot-logs-YYYY.MM.dd`).
6. **Kibana** is used to visualize logs and dashboards.

## Project Structure

```text
spring-boot-elk-logging/
├── docker/
│   └── logstash/
│       ├── config/
│       │   └── logstash.yml
│       └── pipeline/
│           └── logstash.conf
├── src/
│   ├── main/
│   │   ├── java/com/example/elk/
│   │   │   ├── controller/LogController.java
│   │   │   ├── service/LogGeneratorService.java
│   │   │   └── ElkLoggingApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── log4j2.xml
│   └── test/java/com/example/elk/ElkLoggingApplicationTests.java
├── docker-compose.yml
├── pom.xml
└── README.md
```


## Microservices Expansion Guide

If you want to evolve this project into a full microservices platform with **Eureka**, **API Gateway**, **user/order/payment services**, and **Elasticsearch-backed search**, follow:

- [`docs/microservices-elk-step-by-step.md`](docs/microservices-elk-step-by-step.md)

This guide is organized as an implementation sequence, with suggested service boundaries, configs, and validation checklist.

## Maven Dependencies

Main dependencies included in `pom.xml`:

- `spring-boot-starter-web`
- `spring-boot-starter-log4j2`
- `spring-boot-starter-actuator`
- `spring-boot-starter-test`

> Note: `spring-boot-starter-logging` is excluded from web starter to avoid Logback conflict.

## Logging Configuration (`log4j2.xml`)

- Console appender for local visibility.
- TCP `Socket` appender sending logs to Logstash (`LOGSTASH_HOST`, `LOGSTASH_PORT`).
- Pattern designed for Grok parsing:

```text
%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - app=spring-boot-elk-logging %msg
```

## Logstash Pipeline (`logstash.conf`)

- `input` uses TCP on port `5000`.
- `grok` extracts timestamp, thread, level, logger, app, and log message.
- `date` filter maps parsed timestamp into `@timestamp`.
- `output` sends data to Elasticsearch index `spring-boot-logs-%{+YYYY.MM.dd}`.

## Docker Setup (ELK)

Start Elasticsearch, Logstash, and Kibana:

```bash
docker compose up -d
```

Services:

- Elasticsearch: `http://localhost:9200`
- Logstash TCP input: `localhost:5000`
- Kibana: `http://localhost:5601`

## Run Spring Boot App

Make sure Java 21 is installed.

```bash
mvn clean spring-boot:run
```

To explicitly set Logstash target (if needed):

```bash
LOGSTASH_HOST=localhost LOGSTASH_PORT=5000 mvn spring-boot:run
```

## Sample REST APIs to Generate Logs

Use these endpoints to generate logs:

```bash
curl "http://localhost:8080/api/logs/info?source=manual-test"
curl "http://localhost:8080/api/logs/warn?source=manual-test"
curl "http://localhost:8080/api/logs/error?source=manual-test"
```

## Kibana Setup and Dashboard

1. Open Kibana: `http://localhost:5601`.
2. Go to **Stack Management → Data Views**.
3. Create data view:
   - Name: `spring-boot-logs-*`
   - Index pattern: `spring-boot-logs-*`
   - Timestamp field: `@timestamp`
4. Open **Discover** and filter logs (e.g., by `log_level`, `app_name`, `log_message`).
5. Build visualizations, e.g.:
   - Log count over time.
   - Top log levels.
   - Top logger classes.
6. Save visualizations and add them to a dashboard.

## Step-by-Step Quick Start

1. Clone the project.
2. Run ELK stack:
   ```bash
   docker compose up -d
   ```
3. Verify Elasticsearch health:
   ```bash
   curl http://localhost:9200/_cluster/health?pretty
   ```
4. Start Spring Boot app:
   ```bash
   mvn clean spring-boot:run
   ```
5. Generate logs via API calls (`/info`, `/warn`, `/error`).
6. In Kibana, create data view `spring-boot-logs-*` and explore logs in Discover.

## Useful Commands

```bash
# show Elasticsearch indices
curl "http://localhost:9200/_cat/indices?v"

# inspect Logstash logs
docker logs -f logstash

# stop ELK
docker compose down
```
