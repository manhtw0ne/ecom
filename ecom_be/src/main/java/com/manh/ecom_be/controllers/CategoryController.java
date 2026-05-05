package com.manh.ecom_be.controllers;


import com.manh.ecom_be.responses.ResponseObject;
import com.manh.ecom_be.services.category.InterfaceCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final InterfaceCategoryService categoryService;

    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllCategories() {
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Get categories successfully")
                .data(categoryService.getAllCategories())
                .status(HttpStatus.OK).build());
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> createCategory(
            @Valid @RequestBody CategoryDTO dto, BindingResult result
    ) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message(result.getFieldErrors().stream()
                            .map(FieldError::getDefaultMessage)
                            .collect(Collectors.joining(", ")))
                    .build());
        }
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Create category successfully")
                .status(HttpStatus.CREATED)
                .data(categoryService.createCategory(dto))
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> updateCategory(
            @PathVariable Long id, @RequestBody CategoryDTO dto
    ) {
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Update category successfully")
                .data(categoryService.updateCategory(id, dto))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Delete category id " + id + " successfully")
                .build());

    }
}
