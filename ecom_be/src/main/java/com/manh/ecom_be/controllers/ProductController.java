package com.manh.ecom_be.controllers;

import com.manh.ecom_be.dtos.ProductDTO;
import com.manh.ecom_be.models.Product;
import com.manh.ecom_be.models.ProductImage;
import com.manh.ecom_be.models.User;
import com.manh.ecom_be.repositories.ProductRepository;
import com.manh.ecom_be.responses.ProductListResponse;
import com.manh.ecom_be.responses.ProductResponse;
import com.manh.ecom_be.responses.ResponseObject;
import com.manh.ecom_be.services.product.InterfaceProductRedisService;
import com.manh.ecom_be.services.product.InterfaceProductService;
import jakarta.validation.Valid;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final InterfaceProductService productService;
    private final InterfaceProductRedisService productRedisService;
    private final ProductRepository productRepository;

    @GetMapping("")
    public ResponseEntity<ResponseObject> getProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0", name = "category_id") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) throws Exception {

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
            @Valid @RequestBody ProductDTO dto, BindingResult result) throws Exception {
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

    @PostMapping(value = "/uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> uploadImages(
            @PathVariable("id") Long productId,
            @ModelAttribute("files") List<MultipartFile> files) throws Exception {

        List<ProductImage> savedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
                        ResponseObject.builder().message("File too large. Max 10MB").build());
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(
                        ResponseObject.builder().message("File must be an image").build());
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

    @PostMapping("/{productId}/like")
    @PreAuthozire("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> likeProduct(@PathVariable Long productId) throws Exception {
        User loginUser = securityUtils.getLoggedInUser();
        productService.likeProduct(loginUser.getId(), productId);
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Like product successfully")
                .status(HttpStatus.OK)
                .build());
    }

    @DeleteMapping("/{productId}/unlike")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> unlikeProduct(@PathVariable Long productId)
        throws Exception {
        User loginUser = securityUtils.getLoggedInUser();
        productService.unlikeProduct(loginUser.getId(), productId);
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Unlike product successfully")
                .status(HttpStatus.OK)
                .build());
    }

    @GetMapping("/{userId}/favorites")
    public ResponseEntity<ResponseObject> getFavoriteProducts(@PathVariable Long userId) {
        List<Product> favorites = productRepository.findFavoriteProductsByUserId(userId);
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Get favorites successfully")
                .data(favorites.stream().map(ProductResponse::fromProduct).toList())
                .status(HttpStatus.OK)
                .build());
    }
}



