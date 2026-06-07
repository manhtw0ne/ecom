# Ecom - Spring Boot E-commerce Application

Dự án này là một ứng dụng thương mại điện tử (E-commerce) hoàn chỉnh bao gồm Backend (Spring Boot) và Frontend (Angular). Trong dự án này, phần giao diện người dùng (Frontend) được tạo và đồng bộ tự động chủ yếu bằng công cụ trí tuệ nhân tạo (AI). Trọng tâm phát triển cốt lõi của dự án tập trung vào việc thiết kế cấu trúc hệ thống, cài đặt dịch vụ và tối ưu hóa hiệu năng ở phần Backend.

---

## Các tính năng và công nghệ đã triển khai ở Backend

Dưới đây là chi tiết các thành phần hệ thống đã được thiết kế và lập trình ở Backend:

### 1. Cơ sở dữ liệu và quản lý phiên bản cơ sở dữ liệu (Flyway)
- Sử dụng cơ sở dữ liệu quan hệ MySQL để quản lý thông tin sản phẩm, đơn hàng, tài khoản người dùng và lịch sử giao dịch.
- Tích hợp Flyway Migration để quản lý phiên bản mã nguồn cơ sở dữ liệu theo thời gian, giúp việc nâng cấp schema diễn ra tự động và an toàn.

### 2. Quản lý sản phẩm, danh mục và bộ đệm Redis
- Thiết lập các API CRUD cho sản phẩm (Products) và danh mục (Categories).
- Tích hợp Redis Cache (sử dụng thư viện Lettuce) để lưu trữ bộ đệm danh sách sản phẩm. Khi có yêu cầu tìm kiếm hoặc lọc danh sách, dữ liệu được trả về trực tiếp từ bộ đệm giúp giảm tải cho cơ sở dữ liệu và cải thiện tốc độ tải trang.

### 3. Hệ thống đặt hàng và tích hợp cổng thanh toán VNPay
- Phát triển quy trình xử lý đơn hàng (Orders) và chi tiết đơn hàng (Order Details).
- Tích hợp cổng thanh toán trực tuyến VNPay Sandbox, cho phép xử lý tạo yêu cầu thanh toán, nhận callback phản hồi từ ngân hàng và xử lý cập nhật trạng thái đơn hàng tự động.

### 4. Hệ thống mã giảm giá (Coupon System) theo mô hình EAV
- Thiết kế mô hình cơ sở dữ liệu linh hoạt dựa trên kiến trúc EAV (Entity-Attribute-Value) để quản lý các điều kiện áp dụng mã giảm giá (ví dụ: áp dụng theo tổng giá trị đơn hàng tối thiểu, theo danh mục sản phẩm, v.v.).
- Xử lý logic tính toán giá trị giảm giá động cho các đơn hàng.

### 5. Ghi nhật ký hệ thống (AOP Logging) và truyền tin bất đồng bộ (Kafka)
- Sử dụng khía cạnh lập trình hướng khía cạnh (AOP) để tự động ghi nhận thời gian thực thi (Performance Aspect Logging) của các controller và service mà không làm ảnh hưởng đến mã nguồn nghiệp vụ chính.
- Tích hợp Apache Kafka để xử lý sự kiện bất đồng bộ (Async Events) như đẩy tin khi có danh mục mới hoặc yêu cầu đồng bộ danh mục sản phẩm, giúp tăng tính chịu tải và giảm thời gian phản hồi cho client.

### 6. Đánh giá, yêu thích sản phẩm và đa ngôn ngữ (i18n)
- Triển khai tính năng bình luận (Comments) và danh sách sản phẩm yêu thích (Favorites) gắn liền với từng tài khoản người dùng.
- Tích hợp cơ chế bản địa hóa đa ngôn ngữ (i18n) hỗ trợ tiếng Anh (en) và tiếng Việt (vi) cho các thông điệp phản hồi từ hệ thống và lỗi xác thực dữ liệu đầu vào.

### 7. Bảo mật và API Documentation
- Thiết lập hệ thống bảo mật bằng Spring Security, cơ chế xác thực không lưu trạng thái (Stateless) qua mã thông báo JWT (JSON Web Token), bao gồm xử lý gia hạn mã thông báo (Refresh Token).
- Hỗ trợ đăng nhập qua mạng xã hội (OAuth2 Login) đối với tài khoản Google và Facebook.
- Tự động tài liệu hóa các API bằng Swagger UI / OpenAPI để hỗ trợ quá trình tích hợp.
- Cấu hình Spring Boot Actuator phục vụ giám sát trạng thái sức khỏe (Health Check) hệ thống.

---

## Phần Frontend (Angular)

Giao diện Frontend của ứng dụng được xây dựng trên nền tảng Angular v19, sử dụng Bootstrap làm framework giao diện chính.
- Toàn bộ giao diện được sinh bằng AI thông qua việc cấu hình và ánh xạ trực tiếp từ các mô hình API được thiết kế từ Backend.
- Đã được chuẩn hóa để loại bỏ lỗi bất đồng bộ liên quan đến hydrate tĩnh trên môi trường SSR (Server-Side Rendering) và biên dịch (build) thành công 15 router tĩnh.

---

## Hướng dẫn cài đặt và khởi chạy cục bộ

### 1. Yêu cầu hệ thống
- Java Development Kit (JDK) phiên bản 21.
- Node.js (phiên bản 18 trở lên) và trình quản lý gói pnpm.
- Docker Desktop.
- MySQL Server (nếu chạy trực tiếp trên Windows) hoặc sử dụng qua Docker.

### 2. Khởi chạy hạ tầng (Redis & Kafka)
Khởi động các dịch vụ Redis và Kafka bằng Docker Compose ở thư mục gốc:
```bash
docker compose -f docker-compose-infra.yml up -d
```

### 3. Khởi chạy Backend
Di chuyển vào thư mục backend và chạy ứng dụng Spring Boot:
```bash
cd ecom_be
$env:SERVER_PORT="8088"
.\mvnw.cmd spring-boot:run
```
Ứng dụng backend sẽ khởi chạy và lắng nghe ở địa chỉ: http://localhost:8088/api/v1

### 4. Khởi chạy Frontend
Di chuyển vào thư mục frontend, cài đặt thư viện và khởi động dev-server:
```bash
cd ecom_fe
pnpm install
pnpm run start:dev
```
Giao diện người dùng sẽ chạy tại địa chỉ: http://localhost:4300/
