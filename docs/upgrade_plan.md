# 🚀 Kế Hoạch Nâng Cấp Backend E-Commerce — Pass Intern Edition

> **Mục tiêu:** Biến project từ "bài tập cá nhân" thành "sản phẩm chuyên nghiệp" đủ sức gây ấn tượng trong buổi phỏng vấn Intern Backend.

---

## 📊 Đánh Giá Hiện Trạng (As-Is)

| Tiêu chí | Trạng thái hiện tại | Đánh giá |
|---|---|---|
| Kiến trúc tổng thể | Monolithic MVC cơ bản | ⚠️ Thiếu tách lớp rõ ràng |
| Testing | Chỉ có 1 file test rỗng (`EcomBeApplicationTests.java`) | ❌ Không có test |
| Error Handling | `GlobalExceptionHandler` có ~100 dòng comment thừa, chỉ handle 2 exception | ❌ Thiếu chuyên nghiệp |
| Code Quality | Không có Checkstyle/SpotBugs, code không nhất quán | ⚠️ Cần cải thiện |
| CI/CD | Không có GitHub Actions workflow | ❌ Không có |
| API Documentation | Có Swagger nhưng chưa enrich annotations | ⚠️ Cơ bản |
| Security | JWT + OAuth2 đã có | ✅ Tốt |
| Caching (Redis) | Cache-Aside + JPA Listener | ✅ Tốt |
| Messaging (Kafka) | Có nhưng chỉ log audit đơn giản | ⚠️ Demo level |
| Database Migration | Flyway đã có | ✅ Tốt |
| Monitoring | Actuator có nhưng chưa kết nối Prometheus/Grafana | ⚠️ Chưa hoàn thiện |
| Containerization | Chỉ có docker-compose cho infra | ⚠️ App chưa containerize |
| Logging | Chưa có structured logging | ❌ Thiếu |

---

## 🎯 Nguyên Tắc Nâng Cấp

> **Interviewer tìm kiếm gì ở ứng viên Intern?**
>
> 1. **Tư duy kỹ thuật** — Biết *tại sao* chứ không chỉ biết *cách làm*
> 2. **Coding standards** — Code sạch, có test, có convention
> 3. **Hiểu về production** — Logging, monitoring, error handling, containerization
> 4. **Version control** — Commit message rõ ràng, branch strategy có tổ chức
> 5. **Documentation** — README/Swagger/ADR giải thích được kiến trúc

---

## 📋 Tổng Quan Các Phase

```
Phase 1: Code Cleanup & Professional Standards     ──► Nền tảng chuyên nghiệp
Phase 2: Testing & Quality Assurance               ──► Chứng minh code chạy đúng
Phase 3: Advanced Error Handling & Logging          ──► Sẵn sàng cho production
Phase 4: Monitoring & Observability Stack           ──► Hiểu hệ thống đang chạy thế nào
Phase 5: Performance & Concurrency Hardening        ──► Xử lý được tải thực tế
Phase 6: CI/CD Pipeline & Containerization          ──► Triển khai chuyên nghiệp
```

---

## Phase 1: Code Cleanup & Professional Standards

### 🔧 Nâng cấp gì?

#### 1.1. Dọn dẹp Dead Code & Comment thừa
- **File:** `GlobalExceptionHandler.java` — Xóa ~100 dòng code bị comment-out
- **Toàn bộ project:** Tìm và xóa các `System.out.println()`, code thừa, TODO cũ

#### 1.2. Chuẩn hóa Response Format
- Tạo `ApiResponse<T>` thống nhất với cấu trúc:
  ```json
  {
    "success": true,
    "code": 200,
    "message": "Operation successful",
    "data": { ... },
    "timestamp": "2026-08-25T09:00:00Z"
  }
  ```
- Thay thế các response format không nhất quán hiện tại (`ResponseObject`, `BaseResponse`)

#### 1.3. Áp dụng Service Interface Pattern nhất quán
- Đảm bảo tất cả service đều có **Interface** + **Implementation** (một số đã có, một số chưa)
- Đặt tên chuẩn: `IProductService` → `ProductServiceImpl`

#### 1.4. Bổ sung JPA Auditing đầy đủ
- `BaseEntity` hiện tại dùng `@PrePersist`/`@PreUpdate` thủ công
- **Nâng cấp:** Chuyển sang dùng `@EnableJpaAuditing` + `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy` kết hợp `AuditorAware<String>` lấy từ Security Context

#### 1.5. Implement Soft Delete
- Thêm cột `is_deleted` (boolean) vào các bảng quan trọng (products, users, orders)
- Sử dụng `@SQLDelete` và `@SQLRestriction("is_deleted = false")` của Hibernate
- Viết Flyway migration script cho schema change

### ❓ Tại sao?
- Code comment-out nhiều cho thấy thiếu kinh nghiệm quản lý phiên bản → **Interviewer sẽ hỏi ngay**
- Response format thống nhất là **tiêu chuẩn bắt buộc** khi làm việc nhóm
- JPA Auditing + Soft Delete cho thấy bạn hiểu **vòng đời dữ liệu trong hệ thống thực tế**

### 🎯 Giải quyết vấn đề gì?
- Loại bỏ ấn tượng "code messy" khi interviewer review repository
- Tạo nền tảng chuẩn cho các phase tiếp theo
- Chứng minh bạn biết cách viết code production-ready

### 📌 Git Commit
```bash
git add .
git commit -m "refactor: clean dead code, standardize API response format, implement JPA auditing and soft delete

- Remove ~100 lines of commented-out code in GlobalExceptionHandler
- Create unified ApiResponse<T> wrapper replacing ResponseObject/BaseResponse
- Enable JPA Auditing with AuditorAware for createdBy/lastModifiedBy
- Add soft delete with @SQLDelete and @SQLRestriction on core entities
- Add Flyway migration V{n}__add_soft_delete_columns.sql"
```

---

## Phase 2: Testing & Quality Assurance

### 🔧 Nâng cấp gì?

#### 2.1. Unit Tests cho Service Layer
- **Target:** Đạt ≥ 70% coverage cho service layer
- Viết test cho các service quan trọng nhất:
  - `ProductService` — CRUD + search logic
  - `UserService` — Register, Login, Social Login flow
  - `OrderService` — Đặt hàng, cancel, update status
  - `CouponService` — Tính toán giảm giá (EAV pattern)
- Sử dụng **Mockito** để mock Repository layer
- Sử dụng `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks`

#### 2.2. Integration Tests cho Controller Layer
- Sử dụng `@WebMvcTest` + `MockMvc` để test endpoint
- Test các case: happy path, validation error, authorization, not found
- Ví dụ test cases cho `ProductController`:
  - `GET /products` → 200 + paginated list
  - `POST /products` (no auth) → 401
  - `POST /products` (admin) → 201
  - `GET /products/999` → 404

#### 2.3. Test Configuration
- Thêm `application-test.yml` với H2 in-memory database
- Cấu hình test profile để không cần MySQL/Redis/Kafka khi chạy test

### ❓ Tại sao?
- **0 test = Red flag lớn nhất** khi interview. Interviewer sẽ nghĩ bạn không biết test
- Unit test chứng minh bạn hiểu cách **tách biệt các lớp (separation of concerns)**
- Integration test chứng minh bạn hiểu **luồng đi hoàn chỉnh của request**

### 🎯 Giải quyết vấn đề gì?
- Tự tin trả lời câu hỏi "Em có viết test không? Viết loại test gì?"
- Phát hiện bug sớm khi refactor ở các phase sau
- Chứng minh code thực sự chạy đúng, không chỉ "trông có vẻ đúng"

### 📌 Git Commit
```bash
git add .
git commit -m "test: add unit tests for service layer and integration tests for controllers

- Add unit tests for ProductService, UserService, OrderService, CouponService
- Add integration tests for ProductController, UserController, OrderController
- Configure H2 in-memory database for test profile (application-test.yml)
- Achieve ≥70% code coverage on service layer
- Add Mockito + MockMvc test infrastructure"
```

---

## Phase 3: Advanced Error Handling & Structured Logging

### 🔧 Nâng cấp gì?

#### 3.1. Hoàn thiện Global Exception Handler
- Bỏ comment tất cả handler đang bị disable → kích hoạt lại + refactor
- Thêm xử lý cho:
  - `MethodArgumentNotValidException` → 400 (validation errors dạng field-level)
  - `AccessDeniedException` → 403
  - `ExpiredTokenException` → 401
  - `DataIntegrityViolationException` → 409 (duplicate data)
  - `HttpRequestMethodNotSupportedException` → 405
  - `MaxUploadSizeExceededException` → 413
- Trả về error response chuẩn có `errorCode` + `details`:
  ```json
  {
    "success": false,
    "code": 400,
    "message": "Validation failed",
    "errors": [
      { "field": "name", "message": "must not be blank" },
      { "field": "price", "message": "must be greater than 0" }
    ],
    "timestamp": "2026-08-25T09:00:00Z"
  }
  ```

#### 3.2. Custom Error Codes (Enum)
- Tạo `ErrorCode` enum chứa mã lỗi nghiệp vụ:
  ```java
  PRODUCT_NOT_FOUND(1001, "Product not found", HttpStatus.NOT_FOUND),
  ORDER_CANNOT_CANCEL(2001, "Order cannot be cancelled", HttpStatus.BAD_REQUEST),
  INVALID_COUPON(3001, "Coupon is invalid or expired", HttpStatus.BAD_REQUEST)
  ```
- Frontend/Mobile dev nhìn `errorCode` là biết ngay lỗi gì mà không cần đọc message

#### 3.3. Structured Logging với SLF4J + Logback
- Thay tất cả `System.out.println` bằng `@Slf4j` (Lombok)
- Cấu hình `logback-spring.xml`:
  - **Console:** Pattern format đẹp cho development
  - **File:** Rolling file appender với JSON format cho production
- Thêm **MDC (Mapped Diagnostic Context)** cho mỗi request:
  - `requestId` (UUID) — trace được 1 request xuyên suốt nhiều log line
  - `userId` — biết ai đang thực hiện hành động
  - Tạo `RequestIdFilter` để tự động inject MDC vào mỗi request

### ❓ Tại sao?
- **Error handling chuyên nghiệp** = Sự khác biệt giữa junior biết code và junior biết làm product
- Structured logging là **yêu cầu bắt buộc ở mọi công ty** — không có log = không debug được production
- `requestId` cho thấy bạn hiểu cách trace issue trong **hệ thống phân tán**

### 🎯 Giải quyết vấn đề gì?
- API trả về lỗi rõ ràng, frontend dev không cần đoán
- Khi có bug production, có thể trace được nguyên nhân qua log
- Chứng minh bạn đã nghĩ đến chuyện **vận hành hệ thống**, không chỉ viết code

### 📌 Git Commit
```bash
git add .
git commit -m "feat: implement comprehensive error handling and structured logging

- Activate and refactor GlobalExceptionHandler with 8+ exception types
- Create ErrorCode enum with business-specific error codes
- Replace System.out.println with SLF4J @Slf4j across all classes
- Add logback-spring.xml with console + rolling file appenders
- Implement RequestIdFilter with MDC for distributed request tracing
- Add requestId and userId to every log entry"
```

---

## Phase 4: Monitoring & Observability Stack

### 🔧 Nâng cấp gì?

#### 4.1. Spring Boot Actuator nâng cao
- Expose thêm endpoints: `/metrics`, `/info`, `/prometheus`
- Cấu hình `info` endpoint hiển thị build version, git commit hash
- Tạo **Custom Health Indicator** kiểm tra:
  - MySQL connection
  - Redis connection
  - Kafka broker availability

#### 4.2. Prometheus + Grafana Stack
- Thêm dependency `micrometer-registry-prometheus`
- Bổ sung vào `docker-compose-infra.yml`:
  - **Prometheus** container — scrape metrics từ Spring Boot mỗi 15s
  - **Grafana** container — dashboard trực quan
- Tạo sẵn Grafana dashboard JSON:
  - JVM metrics (heap, GC, threads)
  - HTTP request rate, response time (p50, p95, p99)
  - Database connection pool usage
  - Redis cache hit/miss ratio

#### 4.3. Custom Business Metrics
- Sử dụng Micrometer để đo:
  - `ecom.orders.created` (Counter) — số đơn hàng tạo mới
  - `ecom.payments.success` / `ecom.payments.failed` — tỷ lệ thanh toán
  - `ecom.products.search.duration` (Timer) — thời gian search product
- Interviewer hỏi "Em monitor hệ thống thế nào?" → Mở Grafana dashboard cho xem

### ❓ Tại sao?
- **Monitoring** là thứ tách biệt "coder" với "engineer"
- 90% intern không biết Prometheus/Grafana → **đây là lợi thế cạnh tranh lớn**
- Custom metrics cho thấy bạn hiểu **business logic**, không chỉ tech

### 🎯 Giải quyết vấn đề gì?
- Biết hệ thống đang chạy khỏe hay yếu mà không cần SSH vào server đọc log
- Phát hiện bottleneck (API nào chậm, cache miss nhiều, DB connection đầy)
- Có bằng chứng trực quan khi trình bày trong buổi phỏng vấn

### 📌 Git Commit
```bash
git add .
git commit -m "feat: add Prometheus + Grafana monitoring with custom business metrics

- Configure Spring Boot Actuator with Prometheus endpoint
- Add Prometheus + Grafana to docker-compose-infra.yml
- Create custom health indicators for MySQL, Redis, Kafka
- Implement business metrics: orders.created, payments.success/failed
- Include pre-built Grafana dashboard JSON (JVM + HTTP + Business)"
```

---

## Phase 5: Performance & Concurrency Hardening

### 🔧 Nâng cấp gì?

#### 5.1. Rate Limiting (Bucket4j + Redis)
- Tạo `RateLimitInterceptor` giới hạn request theo IP:
  - Default: 60 requests/phút cho public APIs
  - Login endpoint: 10 requests/phút (chống brute-force)
  - Admin endpoints: 120 requests/phút
- Lưu counter trong Redis (distributed, không bị reset khi restart app)
- Trả về header `X-RateLimit-Remaining` và `Retry-After` khi bị limit

#### 5.2. Distributed Lock với Redisson (Chống Overselling)
- **Bài toán:** 1000 người cùng mua 1 sản phẩm còn 5 cái trong kho
- **Giải pháp:** Dùng Redisson Distributed Lock trước khi trừ stock
  ```
  Lock("product:stock:{productId}") → Check stock → Deduct → Unlock
  ```
- Viết test mô phỏng concurrent requests để chứng minh không bị oversell

#### 5.3. Async Email Notification
- Tích hợp `spring-boot-starter-mail` với Gmail SMTP
- Sử dụng `@EnableAsync` + `@Async` để gửi email không block API response
- Template email cho: Đăng ký thành công, Đặt hàng thành công, Thanh toán thành công
- Cấu hình `TaskExecutor` thread pool riêng cho async tasks

#### 5.4. Redis Cache cải tiến
- Thay vì `FLUSHALL` khi data thay đổi → xóa theo pattern key cụ thể
- Thêm cache cho thêm các entity: Category list, User profile
- Cấu hình TTL phù hợp cho từng loại cache

### ❓ Tại sao?
- **Rate limiting** = Câu hỏi interview kinh điển: "Nếu có người spam API thì sao?"
- **Distributed lock** = Câu hỏi nâng cao: "Xử lý concurrent access thế nào?"
- **Async processing** = Cho thấy bạn hiểu **threading model** của Spring Boot
- Đây là các **kỹ năng mà intern hiếm khi có** → gây ấn tượng mạnh

### 🎯 Giải quyết vấn đề gì?
- Bảo vệ API khỏi spam/DDoS/brute-force
- Đảm bảo tính nhất quán dữ liệu khi có nhiều người mua cùng lúc
- API response nhanh hơn vì email được xử lý background

### 📌 Git Commit
```bash
git add .
git commit -m "feat: add rate limiting, distributed lock, async email and cache improvements

- Implement RateLimitInterceptor with Bucket4j + Redis (60 req/min default)
- Add Redisson distributed lock for product stock deduction (anti-overselling)
- Integrate Spring Mail with @Async for non-blocking email notifications
- Improve Redis cache: pattern-based invalidation instead of FLUSHALL
- Add cache for categories and user profiles with appropriate TTL"
```

---

## Phase 6: CI/CD Pipeline & Containerization

### 🔧 Nâng cấp gì?

#### 6.1. Dockerfile cho ứng dụng
- Multi-stage build:
  - **Stage 1 (Builder):** Maven build + run tests
  - **Stage 2 (Runtime):** JRE-only image, copy JAR, expose port
- Optimize Docker layer caching (copy `pom.xml` trước, `src/` sau)

#### 6.2. Docker Compose hoàn chỉnh
- Cập nhật `docker-compose.yml` bao gồm tất cả services:
  - MySQL + init script
  - Redis
  - Kafka + Zookeeper
  - Prometheus + Grafana
  - **Spring Boot App** (build từ Dockerfile)
- Một lệnh `docker compose up` là chạy được toàn bộ hệ thống

#### 6.3. GitHub Actions CI Pipeline
- Tạo `.github/workflows/ci.yml`:
  ```
  Trigger: Push to main, Pull Request
  Steps:
    1. Checkout code
    2. Setup JDK 21
    3. Cache Maven dependencies
    4. Run tests (mvn test)
    5. Build JAR (mvn package -DskipTests)
    6. Build Docker image
    7. (Optional) Push to Docker Hub / GitHub Container Registry
  ```
- Badge CI status hiển thị trên README

#### 6.4. Environment Profiles
- Tách cấu hình theo profile:
  - `application-dev.yml` — local development
  - `application-test.yml` — test with H2
  - `application-prod.yml` — production settings
- Sử dụng environment variables cho sensitive config (DB password, JWT secret, API keys)

### ❓ Tại sao?
- **CI/CD** cho thấy bạn hiểu **quy trình phát triển phần mềm thực tế**
- **Docker** = Tiêu chuẩn deploy hiện đại, 100% công ty đều dùng
- **Environment profiles** = Cho thấy bạn phân biệt được dev/staging/production
- Badge CI trên README = **First impression chuyên nghiệp** khi interviewer mở repo

### 🎯 Giải quyết vấn đề gì?
- Interviewer clone repo → `docker compose up` → Toàn bộ hệ thống chạy → **WOW**
- Tự động chạy test mỗi khi push code → phát hiện lỗi sớm
- Chứng minh bạn đã sẵn sàng làm việc trong **team thực tế**

### 📌 Git Commit
```bash
git add .
git commit -m "ci: add Dockerfile, complete docker-compose, GitHub Actions CI pipeline

- Create multi-stage Dockerfile for Spring Boot app
- Update docker-compose.yml with all services (MySQL, Redis, Kafka, Prometheus, Grafana, App)
- Add GitHub Actions CI workflow: test → build → docker image
- Split config into dev/test/prod profiles with env variable support
- Add CI status badge to README.md"
```

---

## 📅 Timeline Ước Tính

| Phase | Thời gian | Độ khó | Impact lên Interviewer |
|---|---|---|---|
| Phase 1: Code Cleanup | 2-3 ngày | ⭐⭐ | ⭐⭐⭐ |
| Phase 2: Testing | 3-4 ngày | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Phase 3: Error Handling & Logging | 2-3 ngày | ⭐⭐ | ⭐⭐⭐⭐ |
| Phase 4: Monitoring | 2-3 ngày | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Phase 5: Performance | 3-5 ngày | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Phase 6: CI/CD | 2-3 ngày | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Tổng** | **~14-21 ngày** | | |

---

## 🏆 Kết quả sau khi hoàn thành

Sau 6 phase, project của bạn sẽ có:

```
✅ Code sạch, không dead code, convention nhất quán
✅ 70%+ test coverage với Unit Test + Integration Test
✅ Error handling chuyên nghiệp với error codes + structured logging
✅ Prometheus + Grafana dashboard monitoring real-time
✅ Rate Limiting chống spam + Distributed Lock chống overselling
✅ Async email notifications
✅ Dockerfile multi-stage + Docker Compose one-click deploy
✅ GitHub Actions CI pipeline với badge trên README
✅ JPA Auditing + Soft Delete cho data lifecycle management
✅ Environment profiles (dev/test/prod) tách biệt
```

> **Khi interviewer mở repository của bạn, họ sẽ thấy:**
> - README có CI badge ✅ xanh
> - Cấu trúc code rõ ràng, không dead code
> - Test chạy qua hết
> - `docker compose up` chạy cả hệ thống
> - Grafana dashboard monitoring
>
> → **Đây không phải level intern thông thường. Đây là level "tôi đã sẵn sàng làm việc thực tế."**

---

## 💡 Mẹo Phỏng Vấn

Khi trình bày project, hãy nói theo format:

> **"Em gặp vấn đề X → Em chọn giải pháp Y → Vì lý do Z"**

Ví dụ:
- *"Khi 1000 người cùng mua 1 sản phẩm, em phát hiện có thể bị overselling. Em dùng Redisson Distributed Lock vì nó đảm bảo chỉ 1 thread được trừ stock tại một thời điểm, và lock được lưu trên Redis nên hoạt động tốt khi scale ngang."*
- *"Em thấy việc gửi email đồng bộ khiến API response chậm 2-3 giây. Em dùng @Async để xử lý email ở background thread pool, API trả về 200 ngay lập tức."*
- *"Em tích hợp Prometheus + Grafana để biết API nào chậm, cache hit rate bao nhiêu, và có thể demo trực tiếp dashboard cho interviewer xem."*
