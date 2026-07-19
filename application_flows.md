# Tài Liệu Luồng Xử Lý - E-Commerce Application

> **Stack:** Angular 17 (FE :4300)  ←→  Spring Boot 3 (BE :8088)  ←→  MySQL / Redis / Kafka / VNPay

---

## Mục lục

- [1. Luồng Đăng Nhập Mạng Xã Hội (Social Login OAuth2)](#1-social-login)
- [2. Luồng Đặt Hàng & Thanh Toán VNPay](#2-vnpay)
- [3. Luồng Cache Sản Phẩm với Redis](#3-redis-cache)
- [4. Luồng Upload & Hiển Thị Hình Ảnh](#4-file-upload)
- [5. Luồng Kafka - Category Events](#5-kafka)
- [6. Tổng quan Kiến trúc Hệ thống](#6-architecture)

---

## 1. Luồng Đăng Nhập Mạng Xã Hội (Social Login OAuth2) {#1-social-login}

Hỗ trợ: **Đăng nhập truyền thống** (email + password) và **OAuth2** (Google / Facebook).

```
NGƯỜI DÙNG        FRONTEND (Angular)         BACKEND (Spring Boot)      GOOGLE / FACEBOOK
    │                     │                           │                         │
    │  Click "Login with  │                           │                         │
    │──Google/Facebook"──►│                           │                         │
    │                     │ GET /users/social-login   │                         │
    │                     │──?login_type=google──────►│                         │
    │                     │                           │  Tạo Authorization URL  │
    │                     │◄──── Redirect URL ────────│                         │
    │                     │                           │                         │
    │◄── Redirect trình ──│                           │                         │
    │    duyệt đến        │                           │                         │
    │    Consent Screen ──────────────────────────────────────────────────────►│
    │                     │                           │                         │
    │  Đăng nhập &        │                           │                         │
    │  đồng ý cấp quyền ──────────────────────────────────────────────────────►│
    │                     │                           │                         │
    │                     │◄── Callback: /auth/google/callback?code=xyz ────────│
    │                     │                           │                         │
    │                     │ POST /auth/social/callback│                         │
    │                     │──?code=xyz&login_type=g──►│                         │
    │                     │                           │──── Đổi code lấy ──────►│
    │                     │                           │     User Profile         │
    │                     │                           │◄── Email,Name,Picture ──│
    │                     │                           │                         │
    │                     │                           │  [Tạo/tìm User trong DB]│
    │                     │                           │  [Sinh JWT + Refresh    │
    │                     │                           │   Token]                │
    │                     │◄── JWT + Refresh Token ───│                         │
    │                     │                           │                         │
    │                     │ GET /users/details        │                         │
    │                     │── (Bearer JWT) ──────────►│                         │
    │                     │◄── Chi tiết User Profile ─│                         │
    │                     │                           │                         │
    │                     │  [Lưu Token vào           │                         │
    │                     │   LocalStorage]           │                         │
    │◄─ Về Home / Admin ──│                           │                         │
    │                     │                           │                         │
```

**Lớp dịch vụ liên quan:**
| Class | Vai trò |
|---|---|
| `UserController` | Endpoint `/social-login`, `/auth/social/callback` |
| `AuthService` | Tạo Authorization URL, gọi OAuth Provider |
| `UserService.loginSocial()` | Tạo/tìm User, sinh JWT |
| `TokenService.addToken()` | Lưu JWT + Refresh Token vào DB |
| `AuthCallbackComponent` (FE) | Nhận `code`, gọi BE, lưu LocalStorage |

---

## 2. Luồng Đặt Hàng & Thanh Toán VNPay {#2-vnpay}

**Cơ chế liên kết:** BE sinh `vnp_TxnRef` ngẫu nhiên → lưu vào cột `orders.vnp_txn_ref` → VNPay gửi lại `vnp_TxnRef` khi callback → FE dùng để cập nhật trạng thái đơn hàng.

```
NGƯỜI DÙNG        FRONTEND (Angular)     BACKEND (Spring Boot)    MySQL DB      VNPAY SANDBOX
    │                    │                       │                    │               │
    │  Bấm "Đặt hàng"   │                       │                    │               │
    │  (chọn VNPay) ────►│                       │                    │               │
    │                    │ POST /payments/        │                    │               │
    │                    │ create_payment_url     │                    │               │
    │                    │─── {amount: 500000} ──►│                    │               │
    │                    │                       │  Sinh vnp_TxnRef   │               │
    │                    │                       │  (8 ký tự random)  │               │
    │                    │                       │  Tạo signed URL    │               │
    │                    │◄── Payment URL ────────│                    │               │
    │                    │    (chứa vnp_TxnRef)   │                    │               │
    │                    │                       │                    │               │
    │                    │  [Tách vnp_TxnRef     │                    │               │
    │                    │   từ URL]             │                    │               │
    │                    │                       │                    │               │
    │                    │ POST /orders           │                    │               │
    │                    │─── {cart, vnp_txn_ref}►│                    │               │
    │                    │                       │── INSERT Order ────►│               │
    │                    │                       │   status=pending    │               │
    │                    │                       │   vnp_txn_ref=...   │               │
    │                    │◄── 200 OK ─────────────│                    │               │
    │                    │                       │                    │               │
    │◄─ Redirect đến ────│                       │                    │               │
    │   VNPay Sandbox ──────────────────────────────────────────────────────────────►│
    │                    │                       │                    │               │
    │  Nhập thẻ &        │                       │                    │               │
    │  thanh toán ──────────────────────────────────────────────────────────────────►│
    │                    │                       │                    │               │
    │                    │◄──── Callback: /payments/payment-callback ─────────────────│
    │                    │      ?vnp_ResponseCode=00&vnp_TxnRef=XYZ                   │
    │                    │                       │                    │               │
    │   ┌─── Thanh toán thành công (ResponseCode == "00") ──────────────────────┐    │
    │   │                │                       │                    │          │    │
    │   │                │ PUT /orders/XYZ/status │                    │          │    │
    │   │                │─── ?status=shipped ───►│                    │          │    │
    │   │                │                       │── Tìm theo ────────►│          │    │
    │   │                │                       │   vnp_txn_ref       │          │    │
    │   │                │                       │◄── Order found ─────│          │    │
    │   │                │                       │── UPDATE status ────►│          │    │
    │   │                │                       │   = shipped         │          │    │
    │   │                │◄─── 200 OK ────────────│                    │          │    │
    │   │                │  [Xóa giỏ hàng]       │                    │          │    │
    │   │◄─ Về Home ─────│                       │                    │          │    │
    │   └────────────────────────────────────────────────────────────────────────┘    │
    │                    │                       │                    │               │
    │   ┌─── Thanh toán thất bại (ResponseCode != "00") ─────────────────────────┐   │
    │   │◄─ Hiển thị lỗi,│                       │                    │           │   │
    │   │  về trang ──────│                       │                    │           │   │
    │   │  Checkout ──────│                       │                    │           │   │
    │   └────────────────────────────────────────────────────────────────────────┘   │
```

**Trạng thái đơn hàng (Order Status):**
```
pending  ──►  shipped  ──►  delivered
   │
   └──►  cancelled  (user cancel, chỉ khi còn ở pending)
```

---

## 3. Luồng Cache Sản Phẩm với Redis {#3-redis-cache}

**Chiến lược:** Cache-Aside — kiểm tra Redis trước, chỉ query MySQL khi Redis trống.
**Đồng bộ cache:** JPA Lifecycle Listener tự động xóa cache khi dữ liệu thay đổi.

### 3.1 Luồng truy vấn danh sách sản phẩm

```
   [FE gọi GET /products?page=0&limit=10&keyword=...]
                        │
                        ▼
            ┌─── Kiểm tra Redis ───┐
            │  productRedisService │
            │  .getAllProducts()   │
            └──────────┬──────────┘
                       │
           ┌───────────┴────────────┐
           │                        │
    CACHE HIT                  CACHE MISS
    (Redis có data)            (Redis trống)
           │                        │
           ▼                        ▼
   Deserialize JSON          Query MySQL DB
   → List<Product>           via ProductRepository
           │                        │
           │                        ▼
           │                  Serialize → JSON
           │                  lưu vào Redis
           │                        │
           └───────────┬────────────┘
                       │
                       ▼
            Trả về List<ProductResponse>
                  cho Frontend
```

### 3.2 Luồng đồng bộ cache khi Admin thay đổi sản phẩm

```
   Admin: Thêm / Sửa / Xóa sản phẩm
                    │
                    ▼
         [JPA lưu vào MySQL DB]
                    │
                    ▼
       JPA Entity Lifecycle Events
   ┌───────────────────────────────┐
   │  @PostPersist  (sau khi thêm) │
   │  @PostUpdate   (sau khi sửa)  │
   │  @PostRemove   (sau khi xóa)  │
   └────────────────┬──────────────┘
                    │
                    ▼
         ProductListener.java
         gọi: productRedisService.clear()
                    │
                    ▼
         Redis FLUSHALL
         (Xóa toàn bộ cache cũ)
                    │
                    ▼
         ✓ Cache đã đồng bộ
         Lần query tiếp theo sẽ lấy
         data mới nhất từ MySQL
```

**Cấu hình Redis trong `application.yml`:**
```yaml
spring.data.redis:
  host: localhost
  port: 6379
  use-redis-cache: true   # false = tắt cache, query thẳng DB
```

---

## 4. Luồng Upload & Hiển Thị Hình Ảnh {#4-file-upload}

### 4.1 Luồng Upload ảnh (Profile Image / Product Image)

```
   [POST /users/upload-profile-image  hoặc  POST /products/uploads]
                          │
                          ▼
              ┌─── Validate 1: File rỗng? ───┐
              │                               │
             Không                           Có
              │                               │
              │                               ▼
              │                    400 Bad Request
              │                    "Image file is required"
              ▼
   ┌─── Validate 2: Dung lượng > 10MB? ───┐
   │                                       │
  Không                                   Có
   │                                       │
   │                                       ▼
   │                            413 Payload Too Large
   │                            "File size exceeds 10MB"
   ▼
   ┌─── Validate 3: Đúng định dạng ảnh? ───┐
   │   (FileUtils.isImageFile — check MIME) │
   │                                        │
  Đúng                                    Sai
   │                                        │
   │                                        ▼
   │                             415 Unsupported Media Type
   │                             "Must be an image file"
   ▼
   Đổi tên file → UUID random
   (VD: a3f2b1c4-xxxx-xxxx-xxxx.jpg)
                │
                ▼
   Lưu vào thư mục  /uploads/
                │
                ▼
   Cập nhật tên file vào DB
   (cột profile_image hoặc thumbnail)
                │
                ▼
   201 Created — Upload thành công ✓
```

### 4.2 Luồng hiển thị ảnh (với Fallback tránh lỗi 500)

```
   [GET /users/profile-images/{imageName}]
   [GET /products/images/{imageName}     ]
                    │
                    ▼
        Tìm file trong /uploads/{imageName}
                    │
        ┌───────────┴────────────┐
        │                        │
    File tồn tại           File KHÔNG tồn tại
        │                   (bị xóa / sai tên)
        │                        │
        ▼                        ▼
   Trả về ảnh gốc        Fallback: Trả về ảnh mặc định
   200 OK                - default-profile-image.jpeg
   MediaType: image/jpeg - notfound.jpeg
                         200 OK (không báo lỗi 500!)
```

> **Lý do có Fallback:** Tránh giao diện người dùng bị vỡ khi file vật lý bị mất hoặc chưa có dữ liệu.

---

## 5. Luồng Kafka - Category Events {#5-kafka}

Kafka được dùng để **phát sự kiện** (Event Publishing) khi danh mục thay đổi. Hiện tại dùng để audit log — có thể mở rộng sang microservices sau.

```
  ┌──────────────────────────────────────────────────────────────────────┐
  │                     SPRING BOOT APPLICATION                          │
  │                                                                      │
  │  ┌─────────────────────────┐                                         │
  │  │   CategoryController    │  PRODUCER                               │
  │  │                         │                                         │
  │  │  POST /categories       │──► kafkaTemplate.send(                  │
  │  │  (Admin tạo category)   │      "insert-a-category", category)     │
  │  │                         │                                         │
  │  │  GET /categories        │──► kafkaTemplate.send(                  │
  │  │  (Lấy danh sách)        │      "get-all-categories", categories)  │
  │  └─────────────────────────┘                                         │
  │              │                                                       │
  └──────────────┼───────────────────────────────────────────────────────┘
                 │
                 ▼
  ┌──────────────────────────────────────┐
  │         KAFKA BROKER                 │
  │         localhost:9092               │
  │                                      │
  │  Topic: "insert-a-category"  ─────┐  │
  │  Topic: "get-all-categories" ───┐ │  │
  └─────────────────────────────────┼─┼──┘
                                    │ │
                 ┌──────────────────┘ │
                 │      ┌─────────────┘
                 ▼      ▼
  ┌──────────────────────────────────────────────────────┐
  │        MyKafkaListener  (groupA)    CONSUMER          │
  │                                                       │
  │  @KafkaHandler                                        │
  │  listenCategory(Category c)                           │
  │  → "Received: " + category                           │
  │                                                       │
  │  @KafkaHandler                                        │
  │  listenListOfCategories(List<Category> list)          │
  │  → "Received: " + categories                         │
  │                                                       │
  │  @KafkaHandler(isDefault = true)                      │
  │  unknown(Object o)                                    │
  │  → "Received unknown: " + object                     │
  └──────────────────────────────────────────────────────┘
```

**Cấu hình Kafka trong `application.yml`:**
```yaml
spring.kafka:
  bootstrap-servers: localhost:9092
  producer:
    value-serializer: JsonSerializer
    type.mapping: "category:com.manh.ecom_be.models.Category"
  consumer:
    value-deserializer: ByteArrayDeserializer
    group-id: ecom_db-group
```

---

## 6. Tổng quan Kiến trúc Hệ thống {#6-architecture}

```
                        ┌─────────────────────────────┐
                        │    Angular Frontend          │
                        │    localhost:4300            │
                        │                             │
                        │  Pages / Components:        │
                        │  ├── Home (Product list)    │
                        │  ├── Detail Product         │
                        │  ├── Login / Register       │
                        │  ├── Order (Checkout)       │
                        │  ├── Detail Order           │
                        │  ├── User Profile           │
                        │  ├── Auth Callback          │
                        │  ├── Payment Callback       │
                        │  └── Admin Panel            │
                        └──────────────┬──────────────┘
                                       │
                          HTTP REST  (Bearer JWT)
                                       │
                        ┌──────────────▼──────────────┐
                        │    Spring Boot Backend       │
                        │    localhost:8088            │
                        │    /api/v1/...               │
                        │                             │
                        │  Controllers:               │
                        │  ├── UserController         │
                        │  ├── ProductController      │
                        │  ├── CategoryController     │
                        │  ├── OrderController        │
                        │  ├── OrderDetailController  │
                        │  ├── CommentController      │
                        │  ├── CouponController       │
                        │  ├── PaymentController      │
                        │  ├── ProductImageController │
                        │  └── HealthCheckController  │
                        └─────────┬──────────┬────────┘
                                  │          │
             ┌────────────────────┤          ├─────────────────────┐
             │                    │          │                     │
             ▼                    ▼          ▼                     ▼
  ┌──────────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐
  │    MySQL DB      │  │    Redis      │  │    Kafka      │  │  External APIs   │
  │  localhost:3306  │  │ localhost:6379│  │ localhost:9092│  │                  │
  │  ecom_db schema  │  │              │  │              │  │  VNPay Sandbox   │
  │                  │  │  Cache:      │  │  Topics:      │  │  Google OAuth2   │
  │  Tables:         │  │  Product     │  │  insert-a-   │  │  Facebook OAuth2 │
  │  ├── users       │  │  list pages  │  │  category    │  │                  │
  │  ├── products    │  │  (JSON)      │  │              │  │                  │
  │  ├── categories  │  │              │  │  get-all-    │  │                  │
  │  ├── orders      │  │              │  │  categories  │  │                  │
  │  ├── order_details│  └──────────────┘  └──────────────┘  └──────────────────┘
  │  ├── tokens      │
  │  ├── coupons     │
  │  ├── comments    │
  │  └── roles       │
  └──────────────────┘
```

### Các endpoint chính (API Prefix: `/api/v1`)

| Resource | Phương thức | Endpoint | Quyền |
|---|---|---|---|
| Auth | POST | `/users/register` | Public |
| Auth | POST | `/users/login` | Public |
| Auth | POST | `/users/refreshToken` | Public |
| Auth | GET | `/users/auth/social-login` | Public |
| Auth | GET | `/users/auth/social/callback` | Public |
| User | POST | `/users/details` | USER / ADMIN |
| User | PUT | `/users/details/{id}` | USER / ADMIN |
| User | POST | `/users/upload-profile-image` | USER / ADMIN |
| Product | GET | `/products` | Public |
| Product | GET | `/products/{id}` | Public |
| Product | POST | `/products` | ADMIN |
| Product | PUT | `/products/{id}` | ADMIN |
| Product | DELETE | `/products/{id}` | ADMIN |
| Category | GET | `/categories` | Public |
| Category | POST | `/categories` | ADMIN |
| Order | POST | `/orders` | USER / ADMIN |
| Order | GET | `/orders/user/{id}` | USER / ADMIN |
| Order | PUT | `/orders/{id}/status` | USER / ADMIN |
| Order | PUT | `/orders/cancel/{id}` | USER |
| Payment | POST | `/payments/create_payment_url` | Public |
| Comment | POST | `/comments` | USER |
| Coupon | GET | `/coupons/calculate` | USER |
| Health | GET | `/actuator/health` | Public |
