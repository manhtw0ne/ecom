package com.manh.ecom_be.controllers;


import com.manh.ecom_be.components.LocalizationUtils;
import com.manh.ecom_be.dtos.CategoryDTO;
import com.manh.ecom_be.models.Category;
import com.manh.ecom_be.responses.ResponseObject;
import com.manh.ecom_be.services.category.InterfaceCategoryService;
import com.manh.ecom_be.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final InterfaceCategoryService categoryService;
    private final KafkaTemplate<?, ?> kafkaTemplate;
    private LocalizationUtils localizationUtils;

    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllCategories() {

        List<Category> categories = categoryService.getAllCategories();

        kafkaTemplate.send("get-all-categories", categories);


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

            return ResponseEntity.ok(ResponseObject.builder()
                    // Dùng localizationUtils thay vì hardcode message
                    .message(localizationUtils.getLocalizedMessage(
                            MessageKeys.INSERT_CATEGORY_SUCCESSFULLY))
                    .data(category)
                    .build());
        }

        Category category = categoryService.createCategory(dto);

        kafkaTemplate.send("insert-a-category", category);

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
