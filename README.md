# AssuranceApi — Insurance Management REST API

A modern Spring Boot REST API for managing insurance quotes, contracts, clients, and products.

## Overview

**AssuranceApi** is a backend service that implements the full lifecycle of an insurance quote-to-contract process:
1. Register clients and products
2. Create quotes (with automatic approval logic for small amounts, manual for large amounts)
3. Manage approval workflows
4. Generate and activate insurance contracts

## Tech Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: H2 in-memory (development); configurable for production
- **Build Tool**: Maven
- **API**: REST/JSON
- **Validation**: Jakarta Bean Validation
- **ORM**: JPA/Hibernate
- **Lombok**: Auto-generate getters, setters, builders

## Project Structure

```
src/main/java/com/baridmedia/assuranceapi/
├── controller/          # REST endpoints
│   ├── ClientController.java
│   ├── ProductController.java
│   ├── QuoteController.java
│   └── ContractController.java
├── service/             # Business logic
│   ├── ClientService.java
│   ├── ProductService.java
│   ├── QuoteService.java
│   └── ContractService.java
├── domain/              # Entities (JPA)
│   ├── Client.java
│   ├── Produit.java
│   ├── Devis.java (Quote)
│   ├── Contrat.java (Contract)
│   ├── QuoteStatus.java
│   └── ContractStatus.java
├── dto/                 # Data Transfer Objects
│   ├── ClientRequestDto / ClientDto
│   ├── ProduitRequestDto / ProduitDto
│   ├── QuoteRequestDto / QuoteDto
│   └── ContractRequestDto / ContractDto
├── repository/          # JPA Repositories
├── exception/           # Custom exceptions + global handler
└── AssuranceApiApplication.java
```

## API Endpoints

### Base URL
```
http://localhost:8080
```



---

## Quick Start

### Prerequisites
- JDK 17+
- Maven 3.8+

### 1. Build and Run

```bash
cd /home/ashraf/IdeaProjects/assuranceApi

# Build
mvn clean package

# Run
mvn spring-boot:run
```

The API will start on `http://localhost:8080`.

### 2. Access H2 Console (Development)

While the app is running:
```
http://localhost:8080/h2-console
```
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

---




