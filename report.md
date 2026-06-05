# Báo Cáo: Đồng Bộ và Cấu Hình Codebase `ecom_fe` Theo `shopapp-angular`

Tài liệu này báo cáo chi tiết các bước đã thực hiện để chuyển đổi cấu trúc thư mục, đồng bộ mã nguồn và cấu hình gói của dự án `ecom_fe` giống hoàn toàn với dự án `shopapp-angular`, giúp ứng dụng hoạt động tương tự mà không gặp lỗi.

---

## 1. Phân Tích Sự Khác Biệt & Các Lỗi Ban Đầu

Khi khảo sát dự án gốc `ecom_fe`, mã nguồn gặp các vấn đề sau:
- **Cấu trúc thư mục chưa đồng bộ**: Các thư mục nguồn (`components`, `services`, `dtos`, `exceptions`, `guards`, `interceptors`, `models`, `responses`, `styles`) nằm trực tiếp dưới thư mục `src/`, trong khi ở dự án mẫu `shopapp-angular` chúng nằm dưới thư mục `src/app/`.
- **Lỗi cú pháp và import**: Nhiều file dịch vụ (ví dụ: `ProductService.ts`, `UserService.ts`) có lỗi cú pháp do import sai hoặc thiếu, thiếu khai báo biến (ví dụ: dùng `provide http: HttpClient` thay vì `private http: HttpClient`, chưa import decorator `@Injectable`, `Observable`, v.v.).
- **Phiên bản Angular không khớp**: `ecom_fe` ban đầu sử dụng các gói Angular v21, trong khi `shopapp-angular` sử dụng Angular v19. Điều này gây lỗi không tương thích phiên bản khi cố chạy hoặc tích hợp mã nguồn gốc.

---

## 2. Các Bước Thực Hiện Để Hoàn Thành Yêu Cầu

### Bước 1: Dọn Dẹp Mã Nguồn Cũ Trong `ecom_fe`
Để đảm bảo không có file rác hoặc các cấu trúc thư mục bị trộn lẫn, các thư mục cũ dưới đây đã được xóa bỏ hoàn toàn khỏi `d:\Desktop\ecom\ecom_fe`:
- Thư mục `src/` cũ.
- Thư mục `public/` cũ (nếu có).

### Bước 2: Sao Chép Mã Nguồn và Cấu Hình Từ `shopapp-angular`
Sao chép đè toàn bộ tài nguyên sạch từ dự án `shopapp-angular` sang `ecom_fe` bao gồm:
- Thư mục `src/` (chứa toàn bộ logic code chuẩn hóa nằm bên trong `src/app/`).
- Thư mục `public/` (chứa các tệp tĩnh, favicon, logo).
- Các tệp cấu hình TypeScript và môi trường:
  - `tsconfig.json`
  - `tsconfig.app.json`
  - `tsconfig.spec.json`
  - `server.ts` (cho Server-Side Rendering)
  - `.editorconfig` (chuẩn hóa định dạng mã nguồn)

### Bước 3: Đổi Tên Gói & Cấu Hình Build sang `ecom-fe`
Để ứng dụng hoạt động độc lập với tên package `ecom-fe` thay vì tên gốc của dự án mẫu:
- **Cấu hình `package.json`**:
  - Đổi `"name"` từ `"shopapp-angular"` thành `"ecom-fe"`.
  - Thay đổi script khởi chạy SSR: `"serve:ssr:shopapp-angular"` thành `"serve:ssr:ecom-fe": "node dist/ecom-fe/server/server.mjs"`.
  - Giữ nguyên các khai báo thư viện phụ thuộc của Angular v19 để đảm bảo ứng dụng chạy ổn định.
- **Cấu hình `angular.json`**:
  - Cập nhật toàn bộ các cấu trúc key từ `"shopapp-angular"` sang `"ecom-fe"`.
  - Cập nhật đường dẫn đầu ra build (output path) thành `"outputPath": "dist/ecom-fe"`.
  - Đổi đích build của dev-server và extract-i18n thành `ecom-fe:build:production` và `ecom-fe:build:development`.

### Bước 4: Đồng Bộ Hóa Trình Quản Lý Gói & Phê Duyệt Build
- Copy tệp khóa thư viện `pnpm-lock.yaml` và `pnpm-workspace.yaml` từ `shopapp-angular` sang để đảm bảo không bị xung đột phiên bản peer-dependency.
- Dọn dẹp thư mục `node_modules/` cũ và thực hiện cài đặt lại toàn bộ thư viện qua lệnh:
  ```bash
  pnpm install
  ```
- Cấp quyền thực thi các script cài đặt cần thiết cho build (`@parcel/watcher`, `esbuild`, `lmdb`, `msgpackr-extract`) qua công cụ hỗ trợ của `pnpm`.

---

## 3. Kết Quả Xác Minh (Build Verification)

Sau khi đồng bộ và sửa đổi tên package, lệnh build production đã được chạy để kiểm tra lỗi:
```bash
pnpm run build
```

**Kết quả thành công**:
- Khởi tạo thành công các browser bundle (`main-4WETSEN2.js`, `styles-UVF7P3UM.css`, `polyfills-U3SDI5WJ.js`).
- Khởi tạo thành công các server bundle cho SSR (`server.mjs`, `polyfills.server.mjs`, v.v.).
- Trình biên dịch đã tiền xử lý thành công **15 static routes** tĩnh (Prerendered 15 static routes) mà không có lỗi thiếu platform (`NG0401`) hay lỗi Router.
- Toàn bộ kết quả build được xuất ra thư mục: `D:\Desktop\ecom\ecom_fe\dist\ecom-fe`.

Ứng dụng hiện tại đã hoạt động tương tự dự án `shopapp-angular` với các phiên bản package và cấu trúc thư mục đồng bộ.
