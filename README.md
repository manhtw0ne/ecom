# Ecom BE - Standalone Spring Boot E-Commerce Backend

This repository contains the standalone, headless Backend (API-Only) of the E-Commerce application. It is built using Spring Boot 3.3.5 and Java 21, implementing modern architectural patterns for high-performance and secure online shopping.

---

## 🚀 Key Features & Technical Implementations

The backend is designed as an API-first service containing the following core systems:

### 1. Database & Schema Migration (Flyway)
- **Database:** MySQL to manage products, categories, orders, order details, tokens, coupons, comments, and roles.
- **Migration:** Integrated **Flyway Migration** to version-control the database schema, ensuring smooth, automated schema creation and updates (`src/main/resources/db/migration`).

### 2. High-Performance Caching (Redis)
- **Strategy:** Cache-Aside strategy using Lettuce client configuration.
- **Implementation:** Product listings are cached in Redis to drastically reduce database query overhead.
- **Cache Synchronization:** Integrated **JPA Entity Lifecycle Listeners** (`@PostPersist`, `@PostUpdate`, `@PostRemove`) in `ProductListener` to automatically flush/invalidate product cache whenever catalog modifications occur.

### 3. Authentication & Authorization (Spring Security 6 & JJWT)
- **JWT Authentication:** Configured stateless request validation via a custom `JwtTokenFilter` using **JJWT 0.12.5** for token signing and validation.
- **Refresh Token Strategy:** Secure refresh token flow storing tokens in the `tokens` table with revocation and expiration checks.
- **Multi-Identifier Login:** Custom `UserDetailsService` allowing users to log in using either their **Phone Number** or **Email**.
- **Social Login:** Configured Google OAuth2 with custom `GoogleOpaqueTokenIntrospector` for API-only token validation.

### 4. Online Payment Gateway (VNPay Sandbox)
- **Flow:** Generates signed payment URLs with HMAC-SHA512 digital signatures.
- **Status Sync:** Handles IPN / Payment Callback responses to securely update order status (from `pending` to `shipped` or `delivered`).

### 5. Event-Driven Messaging (Apache Kafka)
- **Architecture:** Asynchronous messaging to publish Category events (`insert-a-category`, `get-all-categories`).
- **Consumer:** Lobbies messages via `@KafkaListener` to perform background logging/audit logging.

### 6. Flexible Coupon Engine (EAV Pattern)
- **Design:** Implemented the **Entity-Attribute-Value (EAV)** pattern to dynamically compute shopping cart discounts based on varying coupon conditions (e.g., minimum order value, specific categories).

### 7. Diagnostics, API Docs & Internationalization
- **OpenAPI / Swagger:** Auto-generates interactive API documentations at Swagger UI.
- **Spring Boot Actuator:** Out-of-the-box endpoints for system health status.
- **i18n:** Multi-language support for Vietnamese (vi) and English (en) localized API error responses.

---

## 🛠️ Local Development Setup

### 1. Prerequisites
- **Java Development Kit (JDK) 21**
- **Docker Desktop**
- **MySQL Server** (running locally or via Docker)

### 2. Spin Up Infrastructure (Redis & Kafka)
Run the following command at the root directory to launch background Docker containers for Redis, Zookeeper, and Kafka:
```bash
docker compose -f docker-compose-infra.yml up -d
```

### 3. Configure Database
Update connection strings, username, and password in [application.yml](file:///d:/Desktop/ecom_be/src/main/resources/application.yml) if your local MySQL settings differ:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecom_db?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
```

### 4. Run the Application
Run the Spring Boot application using Maven:
- **Windows (PowerShell):**
  ```powershell
  $env:SERVER_PORT="8088"
  .\mvnw.cmd spring-boot:run
  ```
- **macOS/Linux:**
  ```bash
  export SERVER_PORT="8088"
  ./mvnw spring-boot:run
  ```

---

## 📝 API Endpoints & Verification

Once the server starts, you can inspect and interact with the REST APIs directly:

- **Swagger UI (Interactive Docs):** [http://localhost:8088/swagger-ui/index.html](http://localhost:8088/swagger-ui/index.html)
- **Actuator Health Endpoint:** [http://localhost:8088/actuator/health](http://localhost:8088/actuator/health)

---

## 📂 Project Directory Structure

```
ecom_be/
│
├── src/
│   ├── main/
│   │   ├── java/com/manh/ecom_be/
│   │   │   ├── configurations/   # Spring Security, Redis, Kafka, OpenAPI configs
│   │   │   ├── controllers/      # REST API Controllers (endpoints)
│   │   │   ├── services/         # Business logic layer
│   │   │   ├── repositories/     # Database access layer (Spring Data JPA)
│   │   │   ├── models/           # JPA Entities (DB mappings)
│   │   │   ├── dtos/             # Data Transfer Objects
│   │   │   ├── responses/        # API Response formats
│   │   │   ├── filters/          # JwtTokenFilter
│   │   │   └── exceptions/       # Custom Exception & RestControllerAdvice
│   │   │
│   │   └── resources/
│   │       ├── db/migration/     # Flyway SQL migration scripts
│   │       ├── i18n/             # Localized translation properties
│   │       └── application.yml   # Spring Boot application configuration
│   │
│   └── test/                     # Integration and Unit tests
│
├── docker-compose-infra.yml      # Redis, Kafka, Zookeeper docker compose
├── pom.xml                       # Maven dependency manager
└── backend_learning_roadmap.md   # Step-by-step roadmap and practice guide
```
