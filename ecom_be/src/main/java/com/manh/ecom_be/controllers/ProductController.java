package com.manh.ecom_be.controllers;


import com.manh.ecom_be.dtos.ProductDTO;
import com.manh.ecom_be.models.ProductImage;
import com.manh.ecom_be.responses.ProductListResponse;
import com.manh.ecom_be.responses.ProductResponse;
import com.manh.ecom_be.responses.ResponseObject;
import com.manh.ecom_be.services.product.InterfaceProductRedisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final InterfaceService productService;
    private final InterfaceProductRedisService productRedisService;

    @GetMapping("")
    public ResponseEntity<ResponseObject> getProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0", name = "category_id") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) throws Exception {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("id").ascending());
        Page<ProductResponse> productPage =
                productService.getAllProducts(keyword, categoryId, pageRequest);

        return ResponseEntity.ok(ResponseObject.builder()
                .message("Get products successfully")
                .status(HttpStatus.OK)
                .data(ProductListResponse.builder()
                        .products(productPage.getContent())
                        .totalPages(productPage.getTotalPages())
                        .build())
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getProductById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Get product detail successfully")
                .data(ProductResponse.fromProduct(productService.getProductById(id)))
                .build());
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> createProduct(
            @Valid @RequestBody ProductDTO dto, BindingResult result
    ) throws Exception {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message(result.getFieldErrors().stream()
                            .map(FieldError::getDefaultMessage).collect(Collectors.joining("; ")))
                    .status(HttpStatus.BAD_REQUEST).build());
        }
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Create new product successfully")
                .status(HttpStatus.CREATED)
                .data(productService.createProduct(dto))
                .build());
    }
    return ResponseEntity.ok(ResponseObject.builder()
            .message("Create new product successfully")
            .status(HttpStatus.CREATED)
            .data(productService.createProduct(dto))
            .build());
}

@PostMapping(value = "/uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@PreAuthorize("hasRole("ROLE_ADMIN")")
public ResponseEntity<ResponseObject> uploadImages(
        @PathVariable("id") Long productId,
        @ModelAttribute("files") List<MultipartFile> files
) throws Exception {
    List<ProductImage> savedImages = new ArrayList<>();

    for (MultipartFile file : files) {
        if (file.isEmpty()) continue;

        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
                    ResponseObject.builder()
                            .message("File too large. Max 10MB)")
                            .build()
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(
                    ResponseObject.builder().message("File must be an image").build()
            );
        }

        String filename = storeFile(file);

        savedImages.add(productService.createProductImage(
                productId,
                ProductImageDTO.builder().imageUrl(filename).build()
        ));
    }

    return ResponseEntity.ok(ResponseObject.builder()
            .message("Upload images successfully")
            .data(savedImages)
            .build());
}

@PutMapping("/{id}")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public ResponseEntity<ResponseObject> updateProduct(
        @PathVariable Long id, @RequestBody ProductDTO dto
) throws Exception {
    return ResponseEntity.ok(ResponseObject.builder()
            .message("Update product successfully")
            .data(productService.updateProduct(id, dto))
            .build());
}

@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public ResponseEntity<ResponseObject> deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.ok(ResponseObject.builder()
            .message("Delete product id " + id + " successfully")
            .build());
}

@GetMapping("/images/{imageName}")
public ResponseEntity<?> viewImage(@PathVariable String imageName) {
    try {
        java.nio.file.Path imagePath = Paths.get("uploads/" + imageName);
        UrlResource resource = new UrlResource(imagePath.toUri());
        if (resource.exist()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    } catch (Exception e) {
        return ResponseEntity.notFound().build();
    }

    private String storeFile(MultipartFile file) throws IOException {
        String originalName = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename())
        );
        String uniqueName = UUID.randomUUID() + "_" + originalName;

        java.nio.file.Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

        Files.copy(file.getInputStream(),
                uploadDir.resolve(uniqueName), StandardCopyOption.REPLACE_EXISTING);
        return uniqueName;
    }
}
