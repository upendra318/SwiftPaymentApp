# SwiftPaymentApp

## Overview

This repository contains two Spring Boot services:

- `SwiftPayApp`: a payment API service that accepts payment requests, validates input, and publishes events to Kafka.
- `swift-transfer`: a transfer consumer service that reads Kafka payment events and persists them to the database.

The repository also includes Docker, Kubernetes, and load-testing artifacts to support deployment and performance validation.

## Required Software

To build and run this project, install:

- Java 26 JDK
- Maven or use provided `mvnw` / `mvnw.cmd`
- Docker
- Kubernetes CLI (`kubectl`)
- Kafka broker accessible at `localhost:9092`
- PostgreSQL accessible at `localhost:5432`
- `k6` for load testing
- `tcpdump` or packet capture tool for PCAP generation
- Git

## Application Design Steps

1. Start with the Spring Boot application `SwiftPayApp`.
   - It exposes a REST endpoint: `POST /api/v1/payments`.
   - The controller accepts a `PaymentRequest` payload and delegates to `PaymentService`.
2. Validate incoming payment requests.
   - Required fields: `senderId`, `receiverId`, `amount`, `currency`.
   - The service rejects invalid or duplicate input values.
3. Publish a payment event to Kafka.
   - `SwiftPayApp` uses `PaymentProducer` and a Kafka topic named `payment-initiated`.
   - Messages are serialized as JSON and keyed by sender ID.
4. Configure the transfer service `swift-transfer`.
   - It is enabled with `@EnableKafka` and uses a Kafka listener to consume `payment-initiated`.
5. Consume and persist payment events.
   - `PaymentConsumer` receives Kafka events, acknowledges them, and forwards to `SwiftTransferService`.
   - The service validates the event and stores a `Payment` entity.
6. Ensure idempotency and durability.
   - `swift-transfer` checks `messageId` and skips duplicate messages.
   - The consumer persists each payment in the database inside a transactional boundary.
7. Enable deployment support.
   - Dockerfiles are available in each module.
   - Kubernetes manifests are available for both services.
8. Add load test and PCAP capture.
   - `load-test` contains a `k6` script that targets `SwiftPayApp` at 250 TPS.
   - Packet capture helpers generate a PCAP trace during the load test.

## Process Flow

1. Client sends `POST http://localhost:8081/api/v1/payments`.
2. `SwiftPayApp` controller receives the request and validates it.
3. `PaymentServiceImpl` builds a payment event and invokes `PaymentProducer`.
4. Kafka broker receives the event on topic `payment-initiated`.
5. `swift-transfer` consumer listens on the same topic.
6. `PaymentConsumer` receives the event and passes it to `SwiftTransferService`.
7. `SwiftTransferService` validates the payload, checks for duplicate `messageId`, and persists the payment record.
8. The consumer acknowledges the Kafka message after successful processing.

## Module Responsibilities

### SwiftPayApp

- Exposes REST API for payment creation.
- Validates incoming requests.
- Sends Kafka payment events.
- Uses `spring-kafka` and `KafkaTemplate`.
- Listens on port `8081` by default.

### swift-transfer

- Listens to Kafka topic `payment-initiated`.
- Validates and persists payments.
- Uses Spring Data JPA and PostgreSQL.
- Provides duplicate message protection by `messageId`.
- Listens on port `8082` if it exposes HTTP endpoints in the future.

## Deployment Steps

1. Build each module with `./mvnw -B -DskipTests package`.
2. Build Docker images using the module Dockerfiles.
3. Deploy with Kubernetes manifests in each service folder.
4. Confirm Kafka and PostgreSQL services are available.

## Load Testing

- Open `load-test/README.md` for instructions.
- The load test is designed to run at 250 transactions per second for approximately 1,000,000 requests.
- It also captures network traffic to a `payment-loadtest.pcap` file.

## Notes

- The design is based on the service code, configuration, and available deployment artifacts in this repository.
- The system relies on external Kafka and PostgreSQL dependencies for full end-to-end operation.
- The load-test harness is provided to validate throughput and generate the requested PCAP trace.
